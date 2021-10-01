package dev.rikka.tools.refine;

import com.intellij.codeInspection.*;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiModifierList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class RefineForInspectionPlugin extends LocalInspectionTool {
    @Override
    public ProblemDescriptor @Nullable [] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        final ArrayList<ProblemDescriptor> descriptors = new ArrayList<>();

        if (file instanceof PsiJavaFile) {
            for (PsiClass clazz : ((PsiJavaFile) file).getClasses()) {
                final PsiModifierList modifiers = clazz.getModifierList();
                if (modifiers == null) {
                    continue;
                }

                if (!modifiers.hasAnnotation(RefineFor.class.getName())) {
                    continue;
                }

                for (PsiClass inner : clazz.getInnerClasses()) {
                    descriptors.add(manager.createProblemDescriptor(
                            inner,
                            "@RefineFor dont support inner class",
                            (LocalQuickFix) null,
                            ProblemHighlightType.ERROR,
                            isOnTheFly
                    ));
                }
            }
        }

        return descriptors.toArray(new ProblemDescriptor[0]);
    }
}
