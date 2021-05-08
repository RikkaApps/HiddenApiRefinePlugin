package dev.rikka.tools;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import java.util.Collections;

public abstract class HiddenApiRefineExtension {

    public abstract ListProperty<String> getRefinePrefix();

    public abstract Property<Boolean> getLog();

    public HiddenApiRefineExtension() {
        getRefinePrefix().convention(Collections.singleton("$"));
        getLog().convention(true);
    }
}
