package dev.rikka.tools.refine;

import com.android.build.api.instrumentation.InstrumentationScope;
import com.android.build.api.variant.AndroidComponentsExtension;
import com.android.build.api.variant.Component;
import com.android.build.api.variant.HasAndroidTest;
import kotlin.Unit;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Gradle plugin that do the class rename works.
 */
@SuppressWarnings("unused")
public class RefinePlugin implements Plugin<Project> {
    @Override
    public void apply(@Nonnull final Project target) {
        if (!target.getPlugins().hasPlugin("com.android.base")) {
            throw new GradleException("This plugin must be applied after `com.android.application` or `com.android.library`.");
        }

        final AndroidComponentsExtension<?, ?, ?> components = target.getExtensions().getByType(AndroidComponentsExtension.class);
        components.onVariants(components.selector().all(), variant -> {
            configureComponent(variant);
            configureComponent(variant.getUnitTest());
            if (variant instanceof HasAndroidTest) {
                configureComponent(((HasAndroidTest) variant).getAndroidTest());
            }
        });
    }

    private void configureComponent(@Nullable final Component component) {
        if (component != null) {
            component.getInstrumentation().transformClassesWith(
                    RefineFactory.class,
                    InstrumentationScope.ALL,
                    (parameters) -> Unit.INSTANCE
            );
        }
    }
}
