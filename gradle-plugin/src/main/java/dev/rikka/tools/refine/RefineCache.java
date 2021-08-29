package dev.rikka.tools.refine;

import java.util.Map;

public class RefineCache {
    private final Map<String, String> refines;

    public RefineCache(Map<String, String> refines) {
        this.refines = refines;
    }

    public Map<String, String> getRefines() {
        return refines;
    }
}
