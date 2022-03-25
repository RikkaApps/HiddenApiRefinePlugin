package dev.rikka.tools.refine.model;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.impl.light.LightModifierList;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class AnnotatedLightModifierList extends LightModifierList {
    private final PsiAnnotation @NotNull [] annotations;

    public AnnotatedLightModifierList(PsiModifierListOwner modifierListOwner, PsiAnnotation @NotNull [] annotations) {
        super(modifierListOwner);

        this.annotations = annotations;
    }

    @NotNull
    @Override
    public PsiAnnotation @NotNull [] getAnnotations() {
        return annotations;
    }

    @Override
    public boolean hasAnnotation(@NotNull @NonNls String qualifiedName) {
        return Arrays.stream(annotations).anyMatch(a -> a.hasQualifiedName(qualifiedName));
    }

    @Override
    public PsiAnnotation findAnnotation(@NotNull String qualifiedName) {
        return Arrays.stream(annotations).filter(a -> a.hasQualifiedName(qualifiedName)).findFirst().orElse(null);
    }
}
