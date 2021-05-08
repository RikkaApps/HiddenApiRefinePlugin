package org.example;

import org.gradle.api.provider.ListProperty;

public abstract class ClassRenameExtension {
    public abstract ListProperty<String> getPrefixToRemove();
}
