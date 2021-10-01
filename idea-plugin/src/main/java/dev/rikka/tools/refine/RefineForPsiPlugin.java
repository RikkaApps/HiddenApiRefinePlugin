package dev.rikka.tools.refine;

import com.intellij.lang.jvm.annotation.JvmAnnotationClassValue;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.Key;
import com.intellij.psi.*;
import com.intellij.psi.augment.PsiAugmentProvider;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.impl.light.LightMethod;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RefineForPsiPlugin extends PsiAugmentProvider {
    public static final Key<String> KEY_REFINE_CLASS_NAME = Key.create(RefineFor.class.getName());

    private PsiAnnotation kotlinHidden = null;

    @Override
    protected @NotNull <Psi extends PsiElement> List<Psi> getAugments(@NotNull PsiElement element, @NotNull Class<Psi> type, @Nullable String nameHint) {
        if (DumbService.isDumb(element.getProject()))
            return Collections.emptyList();

        if (!type.isAssignableFrom(PsiMethod.class) && !type.isAssignableFrom(PsiField.class))
            return Collections.emptyList();

        if (!(element instanceof PsiClass)) {
            return Collections.emptyList();
        }

        final PsiClass clazz = (PsiClass) element;
        final String clazzName = clazz.getQualifiedName();
        if (clazzName == null) {
            return Collections.emptyList();
        }

        final JavaAnnotationIndex annotations = JavaAnnotationIndex.getInstance();
        final Collection<PsiAnnotation> refines = annotations.get(
                RefineFor.class.getSimpleName(),
                element.getProject(),
                GlobalSearchScope.projectScope(element.getProject())
        );

        if (kotlinHidden == null) {
            kotlinHidden = PsiElementFactory.getInstance(element.getProject())
                    .createAnnotationFromText("@kotlin.Deprecated(message = \"HIDDEN\", level = kotlin.DeprecationLevel.HIDDEN)", clazz);
            System.out.println(kotlinHidden);
        }

        final ArrayList<PsiElement> result = new ArrayList<>();

        for (PsiAnnotation refine : refines) {
            if (!RefineFor.class.getName().equals(refine.getQualifiedName()))
                continue;

            final boolean matched = Arrays.stream(refine.getParameterList().getAttributes())
                    .filter(a -> "value".equals(a.getAttributeName()))
                    .filter(a -> a.getAttributeValue() instanceof JvmAnnotationClassValue)
                    .anyMatch(a -> clazzName.equals(((JvmAnnotationClassValue) a.getAttributeValue()).getQualifiedName()));
            if (matched) {
                if (!(refine.getOwner() instanceof PsiModifierList))
                    continue;

                final PsiModifierList modifiers = (PsiModifierList) refine.getOwner();
                if (!(modifiers.getContext() instanceof PsiClass))
                    continue;

                final PsiClass from = (PsiClass) modifiers.getContext();

                if (type.isAssignableFrom(PsiMethod.class)) {
                    PsiMethod[] allMethods = from.getAllMethods();

                    for (PsiMethod m : allMethods) {
                        m.putUserData(KEY_REFINE_CLASS_NAME, from.getQualifiedName());

                        PsiAnnotation[] annos = new PsiAnnotation[m.getModifierList().getAnnotations().length + 1];
                        System.arraycopy(m.getModifierList().getAnnotations(), 0, annos, 0, m.getModifierList().getAnnotations().length);
                        annos[annos.length - 1] = kotlinHidden;

                        final AnnotatedLightModifierList modifierList = new AnnotatedLightModifierList(
                                m, annos
                        );

                        final LightMethod copied = new AnnotatedLightMethod(
                                m.getManager(),
                                m,
                                from,
                                m.getLanguage(),
                                modifierList
                        );

                        result.add(copied);
                    }
                } else {
                    PsiField[] allFields = from.getAllFields();

                    for (PsiField m : allFields) {
                        m.putUserData(KEY_REFINE_CLASS_NAME, from.getQualifiedName());

                        result.add(m);
                    }
                }
            }
        }

        //noinspection unchecked
        return (List<Psi>) result;
    }
}
