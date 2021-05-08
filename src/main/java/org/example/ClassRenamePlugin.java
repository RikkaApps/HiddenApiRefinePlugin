package org.example;

import com.android.build.api.transform.Transform;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.lang.reflect.Method;
import java.util.function.Function;

public class ClassRenamePlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        if (target.getExtensions().findByName("android") == null)
            throw new GradleException("Extension `android` not found");

        final ClassRenameExtension extension = target.getExtensions()
                .create("classRename", ClassRenameExtension.class);

        final Function<String, String> rename = (String name) -> extension.getPrefixToRemove().get().stream()
                .reduce(name, (String n, String p) -> {
                    if (n.startsWith(p))
                        return n.substring(p.length());
                    return n;
                });

        Object androidExtension = target.getExtensions().getByName("android");

        try {
            Method registerTransform = androidExtension.getClass()
                    .getMethod("registerTransform", Transform.class, Object[].class);

            registerTransform.invoke(
                    androidExtension,
                    new ClassRenameTransform(rename),
                    new Object[0]
            );
        } catch (Exception e) {
            throw new GradleException("Register class rename transform failure", e);
        }
    }
}
