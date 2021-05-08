package dev.rikka.tools;

import com.android.build.gradle.BaseExtension;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.List;
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

        final boolean log = extension.getLog().get();
        final List<String> prefixToRemove = extension.getPrefixToRemove().get();

        final Function<String, String> rename = (String name) -> prefixToRemove.stream()
                .reduce(name, (String n, String p) -> {
                    if (n.startsWith(p)) {
                        String newName = n.substring(p.length());
                        if (log) {
                            System.out.println("Rename class " + n + " to " + newName);
                        }
                        return n.substring(p.length());
                    }
                    return n;
                });

        androidExtension.registerTransform(new HiddenApiRefineTransform(rename));
    }
}
