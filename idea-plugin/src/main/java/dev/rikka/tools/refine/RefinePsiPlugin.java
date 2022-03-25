package dev.rikka.tools.refine;

import com.intellij.lang.jvm.annotation.JvmAnnotationClassValue;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.Key;
import com.intellij.psi.*;
import com.intellij.psi.augment.PsiAugmentProvider;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import dev.rikka.tools.refine.model.AnnotatedLightField;
import dev.rikka.tools.refine.model.AnnotatedLightMethod;
import dev.rikka.tools.refine.model.AnnotatedLightModifierList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class RefinePsiPlugin extends PsiAugmentProvider {
    public static final Key<String> KEY_REFINE_CLASS_NAME = Key.create(RefineFor.class.getName());
    private static final Key<CachedValue<PsiAnnotation>> KEY_HIDDEN_KOTLIN = Key.create(RefinePsiPlugin.class.getName() + ".HiddenKotlin");
    private static final Key<CachedValue<List<PsiMethod>>> KEY_METHODS = Key.create(RefinePsiPlugin.class.getName() + ".Methods");
    private static final Key<CachedValue<List<PsiField>>> KEY_FIELDS = Key.create(RefinePsiPlugin.class.getName() + ".Fields");

    private static AnnotatedLightModifierList createModifierList(PsiModifierListOwner original, PsiAnnotation kotlinHidden) {
        final PsiAnnotation[] originalAnnotations = original.getAnnotations();
        final PsiAnnotation[] annotations = new PsiAnnotation[originalAnnotations.length + 1];
        System.arraycopy(originalAnnotations, 0, annotations, 0, originalAnnotations.length);
        annotations[annotations.length - 1] = kotlinHidden;

        return new AnnotatedLightModifierList(original, annotations);
    }

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

        final JavaAnnotationIndex index = JavaAnnotationIndex.getInstance();
        final Collection<PsiAnnotation> refines = index.get(
                RefineFor.class.getSimpleName(),
                element.getProject(),
                GlobalSearchScope.projectScope(element.getProject())
        );

        final PsiAnnotation kotlinHidden = CachedValuesManager.getCachedValue(clazz, KEY_HIDDEN_KOTLIN, () -> {
            final PsiAnnotation result = PsiElementFactory.getInstance(clazz.getProject())
                    .createAnnotationFromText("@kotlin.Deprecated(message = \"HIDDEN\", level = kotlin.DeprecationLevel.HIDDEN)", clazz);

            return CachedValueProvider.Result.create(result);
        });

        final ArrayList<PsiElement> result = new ArrayList<>();

        for (PsiAnnotation refine : refines) {
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

                final PsiClass refineClass = (PsiClass) modifiers.getContext();

                if (type.isAssignableFrom(PsiMethod.class)) {
                    final List<PsiMethod> methods = CachedValuesManager.getCachedValue(refineClass, KEY_METHODS, () -> {
                        final List<PsiMethod> r = Arrays.stream(refineClass.getAllMethods()).map(m ->
                                new AnnotatedLightMethod(
                                        m.getManager(),
                                        m,
                                        refineClass,
                                        m.getLanguage(),
                                        createModifierList(m, kotlinHidden)
                                )).collect(Collectors.toList());
                        return CachedValueProvider.Result.create(r, PsiModificationTracker.MODIFICATION_COUNT);
                    });
                    result.addAll(methods);
                } else {
                    final List<PsiField> fields = CachedValuesManager.getCachedValue(refineClass, KEY_FIELDS, () -> {
                        final List<PsiField> r = Arrays.stream(refineClass.getAllFields()).map(f ->
                                new AnnotatedLightField(
                                        f.getManager(),
                                        f,
                                        refineClass,
                                        createModifierList(f, kotlinHidden)
                                )).collect(Collectors.toList());
                        return CachedValueProvider.Result.create(r, PsiModificationTracker.MODIFICATION_COUNT);
                    });
                    result.addAll(fields);
                }
            }
        }

        //noinspection unchecked
        return (List<Psi>) result;
    }
}
