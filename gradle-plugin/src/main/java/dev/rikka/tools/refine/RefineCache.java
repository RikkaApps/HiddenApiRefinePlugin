package dev.rikka.tools.refine;

import java.util.List;

public class RefineCache {
    private final List<RefineClass> refines;

    public RefineCache(List<RefineClass> refines) {
        this.refines = refines;
    }

    public List<RefineClass> getRefines() {
        return refines;
    }
}
