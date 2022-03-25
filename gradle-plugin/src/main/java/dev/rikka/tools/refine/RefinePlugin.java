package dev.rikka.tools.refine;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.ForkOptions;
import org.gradle.api.tasks.compile.JavaCompile;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class RefinePlugin implements Plugin<Project> {
    private void decorateJavaCompile(JavaCompile task) {
        final CompileOptions options = task.getOptions();
        if (options.getCompilerArgs() != null && options.getCompilerArgs().contains("-Xplugin:HiddenApiRefine")) {
            return;
        }

        options.setFork(true);
        final ForkOptions forkOptions = options.getForkOptions();
        List<String> jvmArgs = forkOptions.getJvmArgs();
        if (jvmArgs == null) {
            jvmArgs = new ArrayList<>();
        }
        jvmArgs.addAll(Arrays.asList(
                "--add-exports", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
                "--add-exports", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
                "--add-exports", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
                "--add-exports", "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED"
        ));
        forkOptions.setJvmArgs(jvmArgs);

        List<String> compilerArgs = options.getCompilerArgs();
        if (compilerArgs == null) {
            compilerArgs = new ArrayList<>();
        }
        compilerArgs.add("-Xplugin:HiddenApiRefine");
        options.setCompilerArgs(compilerArgs);
    }

    @Override
    public void apply(@Nonnull Project target) {
        target.afterEvaluate(prj -> {
            target.getDependencies().add(
                    "annotationProcessor",
                    "dev.rikka.tools.refine:compiler-plugin:" + RefinePlugin.class.getPackage().getImplementationVersion()
            );

            target.getTasks().withType(JavaCompile.class, this::decorateJavaCompile);

            target.getTasks().whenTaskAdded(task -> {
                if (task instanceof JavaCompile) {
                    decorateJavaCompile((JavaCompile) task);
                }
            });
        });
    }
}
