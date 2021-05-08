package dev.rikka.tools;

import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.internal.pipeline.TransformTask;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.function.Function;

@SuppressWarnings("unused")
public class HiddenApiRefinePlugin implements Plugin<Project> {

    @Override
    public void apply(Project target) {
        BaseExtension androidExtension = target.getExtensions().findByType(BaseExtension.class);
        if (androidExtension == null)
            throw new GradleException("Android extension not found");

        final HiddenApiRefineExtension extension = target.getExtensions()
                .create("hiddenApiRefine", HiddenApiRefineExtension.class);

        final Function<String, String> rename = (String name) -> extension.getRefinePrefix().get().stream()
                .reduce(name, (String n, String p) -> {
                    if (n.startsWith(p)) {
                        String newName = n.substring(p.length());
                        if (extension.getLog().get()) {
                            System.out.println("Rename class " + n + " to " + newName);
                        }
                        return n.substring(p.length());
                    }
                    return n;
                });

        androidExtension.registerTransform(new HiddenApiRefineTransform(rename));

        target.afterEvaluate(project -> project.getTasks().withType(TransformTask.class).whenTaskAdded(task -> {
            if (!(task.getTransform() instanceof HiddenApiRefineTransform)) return;

            task.getInputs().property("log", extension.getLog().get());
            task.getInputs().property("refinePrefix", extension.getRefinePrefix().get());
        }));
    }
}
