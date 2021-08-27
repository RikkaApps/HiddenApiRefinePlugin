package dev.rikka.tools.refine;

import java.util.Map;

public class RefineClass {
    private final String originalClassName;
    private final String replacedClassName;

    public RefineClass(String originalClassName, String replacedClassName) {
        this.originalClassName = originalClassName;
        this.replacedClassName = replacedClassName;
    }

    public String getOriginalClassName() {
        return originalClassName;
    }

    public String getReplacedClassName() {
        return replacedClassName;
    }

    @Override
    public String toString() {
        return "RefineClass{" +
                "originalClassName='" + originalClassName + '\'' +
                ", replacedClassName='" + replacedClassName + '\'' +
                '}';
    }
}
