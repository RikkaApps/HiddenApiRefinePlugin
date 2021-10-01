package dev.rikka.tools.refine;

import com.intellij.lang.Language;
import com.intellij.psi.*;
import com.intellij.psi.impl.light.LightMethod;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Override
    public @Nullable PsiAnnotation getAnnotation(@NotNull @NonNls String fqn) {
        return super.getAnnotation(fqn);
    }
}

