package dev.rikka.tools;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import java.util.Collections;

public abstract class HiddenApiRefineExtension {

    public abstract ListProperty<String> getPrefixToRemove();

    public abstract Property<Boolean> getLog();

    public HiddenApiRefineExtension() {
        getPrefixToRemove().set(Collections.singleton("$"));
        getLog().set(true);
    }
}
