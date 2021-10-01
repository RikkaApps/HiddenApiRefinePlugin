package dev.rikka.tools.refine;

import com.intellij.codeInspection.*;
import com.intellij.lang.jvm.annotation.JvmAnnotationArrayValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttributeValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationClassValue;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;

public class UseRefinesInspectionPlugin extends LocalInspectionTool {
    private static final String USE_REFINES_NAME = UseRefines.class.getName();

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Override
    public ProblemDescriptor @Nullable [] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        if (!(file instanceof PsiJavaFile))
            return null;

        ArrayList<ProblemDescriptor> descriptors = new ArrayList<>();

        for (PsiClass clazz : ((PsiJavaFile) file).getClasses()) {
            final HashSet<String> useRefines = new HashSet<>();
            final PsiModifierList modifiers = clazz.getModifierList();
            if (modifiers != null) {
                if (modifiers.hasModifierProperty(PsiModifier.PUBLIC)) {
                    final PsiAnnotation annotation = modifiers.findAnnotation(USE_REFINES_NAME);
                    if (annotation != null) {
                        for (PsiNameValuePair attribute : annotation.getParameterList().getAttributes()) {
                            if (!attribute.getAttributeName().equals("value"))
                                continue;

                            final JvmAnnotationAttributeValue value = attribute.getAttributeValue();
                            if (value instanceof JvmAnnotationArrayValue) {
                                for (JvmAnnotationAttributeValue target : ((JvmAnnotationArrayValue) value).getValues()) {
                                    if (!(target instanceof JvmAnnotationClassValue)) {
                                        continue;
                                    }

                                    useRefines.add(((JvmAnnotationClassValue) target).getQualifiedName());
                                }
                            } else if (value instanceof JvmAnnotationClassValue) {
                                useRefines.add(((JvmAnnotationClassValue) value).getQualifiedName());
                            }
                        }
                    }
                }
            }

            clazz.acceptChildren(new JavaElementVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    element.acceptChildren(this);
                }

                @Override
                public void visitReferenceExpression(PsiReferenceExpression expression) {
                    final JavaResolveResult result = expression.advancedResolve(false);
                    final PsiElement element = result.getElement();
                    if (element != null) {
                        String refineClass = element.getUserData(RefineForPsiPlugin.KEY_REFINE_CLASS_NAME);
                        if (refineClass != null && !useRefines.contains(refineClass)) {
                            final ProblemDescriptor problem = manager.createProblemDescriptor(
                                    expression,
                                    "Use refined member without @UseRefines",
                                    (LocalQuickFix) null,
                                    ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
                                    isOnTheFly
                            );

                            descriptors.add(problem);
                        }
                    }

                    super.visitReferenceExpression(expression);
                }
            });
        }

        return descriptors.toArray(new ProblemDescriptor[0]);
    }
}
