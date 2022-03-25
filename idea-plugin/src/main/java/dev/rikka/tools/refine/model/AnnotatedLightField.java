package dev.rikka.tools.refine.model;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.impl.light.LightField;
import org.jetbrains.annotations.NotNull;

public class AnnotatedLightField extends LightField {
    private final @NotNull AnnotatedLightModifierList modifierList;

    public AnnotatedLightField(
            @NotNull PsiManager manager,
            @NotNull PsiField field,
            @NotNull PsiClass containingClass,
            @NotNull AnnotatedLightModifierList modifierList
    ) {
        super(manager, field, containingClass);

        this.modifierList = modifierList;
    }

    @Override
    public PsiModifierList getModifierList() {
        return modifierList;
    }
}
