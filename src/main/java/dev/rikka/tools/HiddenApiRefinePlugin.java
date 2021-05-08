package dev.rikka.tools;

import com.android.build.api.transform.Transform;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.lang.reflect.Method;
import java.util.function.Function;

@SuppressWarnings("unused")
public class HiddenApiRefinePlugin implements Plugin<Project> {

    @Override
    public void apply(Project target) {
        if (target.getExtensions().findByName("android") == null)
            throw new GradleException("Extension `android` not found");

        final HiddenApiRefineExtension extension = target.getExtensions()
                .create("hiddenApiRefine", HiddenApiRefineExtension.class);

        final boolean log = extension.getLog().get();

        final Function<String, String> rename = (String name) -> extension.getPrefixToRemove().get().stream()
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

        Object androidExtension = target.getExtensions().getByName("android");

        try {
            Method registerTransform = androidExtension.getClass()
                    .getMethod("registerTransform", Transform.class, Object[].class);

            registerTransform.invoke(
                    androidExtension,
                    new HiddenApiRefineTransform(rename),
                    new Object[0]
            );
        } catch (Exception e) {
            throw new GradleException("Register HiddenApiRefineTransform failed", e);
        }
    }
}
