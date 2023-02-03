package dev.rikka.tools.refine;

import com.android.build.api.instrumentation.InstrumentationScope;
import com.android.build.api.variant.ApplicationAndroidComponentsExtension;
import com.android.build.api.variant.Component;
import com.android.build.api.variant.HasAndroidTest;
import com.android.build.api.variant.LibraryAndroidComponentsExtension;
import com.android.build.api.variant.Variant;

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

        final ApplicationAndroidComponentsExtension applicationComponents =
                target.getExtensions().findByType(ApplicationAndroidComponentsExtension.class);
        if (applicationComponents != null) {
            applicationComponents.onVariants(applicationComponents.selector().all(), variant -> {
                configureVariant(variant);
            });
        }

        final LibraryAndroidComponentsExtension libraryComponents =
                target.getExtensions().findByType(LibraryAndroidComponentsExtension.class);
        if (libraryComponents != null) {
            libraryComponents.onVariants(libraryComponents.selector().all(), variant -> {
                configureVariant(variant);
            });
        }
    }

    private <T extends Variant & HasAndroidTest> void configureVariant(@Nonnull final T variant) {
        configureComponent(variant);
        configureComponent(variant.getUnitTest());
        configureComponent(variant.getAndroidTest());
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
