package dev.rikka.tools.refine.model;

import com.intellij.lang.Language;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.impl.light.LightMethod;
import org.jetbrains.annotations.NotNull;

public class AnnotatedLightMethod extends LightMethod {
    private final AnnotatedLightModifierList modifierList;

    public AnnotatedLightMethod(
            @NotNull PsiManager manager,
            @NotNull PsiMethod method,
            @NotNull PsiClass containingClass,
            @NotNull Language language,
            @NotNull AnnotatedLightModifierList modifierList
    ) {
        super(manager, method, containingClass, language);

        this.modifierList = modifierList;
    }

    @Override
    public @NotNull PsiModifierList getModifierList() {
        return modifierList;
    }
}

