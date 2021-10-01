package dev.rikka.tools.refine;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Pair;

import java.util.HashSet;
import java.util.Set;

public final class AnnotationResolver {
    private final Symbol.ClassSymbol symbolUseRefines;
    private final Symbol.ClassSymbol symbolRefineFor;
    private final Symbol.ClassSymbol symbolRefineName;

    public AnnotationResolver(final Symtab symtab) {
        final Names names = symtab.voidType.tsym.name.table.names;

        symbolUseRefines = Iterables.singleOrNull(symtab.getClassesForName(names.fromString(UseRefines.class.getName())));
        symbolRefineFor = Iterables.singleOrNull(symtab.getClassesForName(names.fromString(RefineFor.class.getName())));
        symbolRefineName = Iterables.singleOrNull(symtab.getClassesForName(names.fromString(RefineName.class.getName())));
    }

    public boolean isValid() {
        return symbolUseRefines != null && symbolRefineFor != null && symbolRefineName != null;
    }

    public Set<Symbol.ClassSymbol> resolveUseRefines(JCTree.JCModifiers modifiers) {
        final Set<Symbol.ClassSymbol> refines = new HashSet<>();

        for (JCTree.JCAnnotation annotation : modifiers.annotations) {
            if (annotation.type.tsym == symbolUseRefines) {
                for (JCTree.JCExpression argument : annotation.args) {
                    if (argument instanceof JCTree.JCAssign) {
                        if (!"value".equals(((JCTree.JCAssign) argument).getVariable().toString())) {
                            continue;
                        }

                        ((JCTree.JCAssign) argument).getExpression().accept(new TreeScanner() {
                            @Override
                            public void visitIdent(JCTree.JCIdent ident) {
                                if (ident.sym instanceof Symbol.ClassSymbol) {
                                    refines.add((Symbol.ClassSymbol) ident.sym);
                                }

                                super.visitIdent(ident);
                            }
                        });
                    }
                }
            }
        }

        return refines;
    }

    public Symbol.ClassSymbol resolveRefineFor(Symbol.ClassSymbol clazz) {
        for (Attribute.Compound annotation : clazz.getAnnotationMirrors()) {
            if (annotation.type.tsym == symbolRefineFor) {
                for (Pair<Symbol.MethodSymbol, Attribute> value : annotation.values) {
                    if (value.fst.name.contentEquals("value")) {
                        if (value.snd instanceof Attribute.Class) {
                            final Symbol symbol = ((Attribute.Class) value.snd).classType.tsym;
                            if (symbol instanceof Symbol.ClassSymbol) {
                                return (Symbol.ClassSymbol) symbol;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    public String resolveRefineName(Symbol symbol) {
        for (Attribute.Compound annotation : symbol.getAnnotationMirrors()) {
            if (annotation.type.tsym == symbolRefineName) {
                for (Pair<Symbol.MethodSymbol, Attribute> value : annotation.values) {
                    if (value.fst.name.contentEquals("value")) {
                        return value.snd.getValue().toString();
                    }
                }
            }
        }

        return null;
    }
}
