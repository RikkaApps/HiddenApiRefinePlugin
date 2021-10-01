package dev.rikka.tools.refine;

import com.google.auto.service.AutoService;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@AutoService(Plugin.class)
public class CompilerPlugin implements Plugin {
    private static Scope.WriteableScope duplicateAndMergeSymbols(
            AnnotationResolver resolver,
            Scope.WriteableScope original,
            Iterable<Scope.WriteableScope> additional,
            ArrayList<Pair<Symbol, Name>> renames
    ) {
        Scope.WriteableScope result = original.dupUnshared(original.owner);

        for (Scope.WriteableScope additionalScope : additional) {
            for (Symbol symbol : additionalScope.getSymbols()) {
                if (symbol instanceof Symbol.MethodSymbol || symbol instanceof Symbol.VarSymbol) {
                    final Symbol duplicated = symbol.clone(original.owner);
                    final String rename = resolver.resolveRefineName(symbol);
                    if (rename != null) {
                        renames.add(new Pair<>(duplicated, symbol.name.table.names.fromString(rename)));
                    }
                    result.enter(duplicated);
                }
            }
        }

        return result;
    }

    @Override
    public String getName() {
        return "HiddenApiRefine";
    }

    @Override
    public void init(JavacTask task, String... args) {
        final Context context = ((BasicJavacTask) task).getContext();

        final Symtab symtab = Symtab.instance(context);

        task.addTaskListener(new TaskListener() {
            private final ArrayList<Pair<Symbol.ClassSymbol, Scope.WriteableScope>> restores = new ArrayList<>();
            private final ArrayList<Pair<Symbol, Name>> renames = new ArrayList<>();
            private AnnotationResolver resolver = null;

            @Override
            public void started(TaskEvent e) {
                try {
                    if (e.getKind() == TaskEvent.Kind.ANALYZE) {
                        if (resolver == null) {
                            resolver = new AnnotationResolver(symtab);
                        }

                        if (!resolver.isValid()) {
                            return;
                        }

                        for (Pair<Symbol.ClassSymbol, Scope.WriteableScope> restore : restores) {
                            restore.fst.members_field = restore.snd;
                        }

                        restores.clear();
                        renames.clear();

                        final JCTree.JCCompilationUnit compilationUnit = (JCTree.JCCompilationUnit) e.getCompilationUnit();
                        final ArrayList<JCTree.JCClassDecl> classes = new ArrayList<>();
                        for (JCTree def : compilationUnit.defs) {
                            if (def instanceof JCTree.JCClassDecl) {
                                classes.add((JCTree.JCClassDecl) def);
                            }
                        }

                        if (classes.size() != 1) {
                            return;
                        }

                        final JCTree.JCClassDecl toplevelClass = classes.get(0);
                        final Set<Symbol.ClassSymbol> useRefines = resolver.resolveUseRefines(toplevelClass.getModifiers());
                        if (useRefines.isEmpty()) {
                            return;
                        }

                        final HashMap<Symbol.ClassSymbol, ArrayList<Scope.WriteableScope>> collected = new HashMap<>();
                        for (Symbol.ClassSymbol refine : useRefines) {
                            refine.complete();

                            final Symbol.ClassSymbol target = resolver.resolveRefineFor(refine);
                            if (target == null) {
                                continue;
                            }
                            target.complete();

                            collected.computeIfAbsent(target, (t) -> new ArrayList<>()).add(refine.members_field);
                        }

                        for (Map.Entry<Symbol.ClassSymbol, ArrayList<Scope.WriteableScope>> entry : collected.entrySet()) {
                            final Scope.WriteableScope merged = duplicateAndMergeSymbols(
                                    resolver,
                                    entry.getKey().members_field,
                                    entry.getValue(),
                                    renames
                            );

                            restores.add(new Pair<>(entry.getKey(), entry.getKey().members_field));

                            entry.getKey().members_field = merged;
                        }
                    } else if (e.getKind() == TaskEvent.Kind.GENERATE) {
                        for (Pair<Symbol, Name> rename : renames) {
                            rename.fst.name = rename.snd;
                        }
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();

                    throw throwable;
                }
            }
        });
    }
}
