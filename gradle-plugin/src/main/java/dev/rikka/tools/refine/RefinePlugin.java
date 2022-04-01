package dev.rikka.tools.refine;

import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

@SuppressWarnings("unused")
public class RefinePlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        target.getPlugins().withId("com.android.base", plugin -> {
            final BaseExtension base = target.getExtensions().getByType(BaseExtension.class);
            base.registerTransform(new RefineTransform(base instanceof LibraryExtension));
        });
    }
}
