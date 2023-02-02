package dev.rikka.tools.refine;

import com.android.build.api.instrumentation.InstrumentationScope;
import com.android.build.api.variant.AndroidComponentsExtension;
import kotlin.Unit;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.annotation.Nonnull;

// FIXME: comments fix

/**
 * Gradle plugin to register transformer.
 */
@SuppressWarnings("unused")
public class RefinePlugin implements Plugin<Project> {
    @Override
    public void apply(@Nonnull final Project target) {
        if (!target.getPlugins().hasPlugin("com.android.base")) {
            // FIXME: message fix
            throw new GradleException("Must apply `com.android.application` or `com.android.library` plugin before");
        }

        final AndroidComponentsExtension<?, ?, ?> components = target.getExtensions().getByType(AndroidComponentsExtension.class);
        components.onVariants(components.selector().all(), variant -> {
            variant.getInstrumentation().transformClassesWith(
                    RefineFactory.class,
                    InstrumentationScope.ALL,
                    (parameters) -> Unit.INSTANCE
            );
        });
    }
}
