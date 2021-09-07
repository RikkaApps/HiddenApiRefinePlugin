package dev.rikka.tools.refine;

import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

@SuppressWarnings("unused")
public class RefinePlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        BaseExtension androidExtension = target.getExtensions().findByType(BaseExtension.class);
        if (androidExtension == null)
            throw new GradleException("Android extension not found");

        boolean isLibrary = androidExtension instanceof LibraryExtension;
        androidExtension.registerTransform(new RefineTransform(isLibrary));
    }
}
