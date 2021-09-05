package dev.rikka.tools.refine;

import com.google.auto.service.AutoService;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.lang.model.util.AbstractTypeVisitor8;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AutoService(Processor.class)
public class RefineAnnotationProcessor extends AbstractProcessor {
    public static final String REFINE_FILE_SUFFIX = ".refine";

    private static String resolveInternalName(Element element) {
        final Element enclosing = element.getEnclosingElement();

        if (enclosing instanceof TypeElement) {
            return resolveInternalName(enclosing) + "$" + element.getSimpleName();
        } else if (enclosing instanceof PackageElement) {
            return ((PackageElement) enclosing).getQualifiedName().toString()
                    .replace('.', '/') + "/" + element.getSimpleName();
        }

        return element.getSimpleName().toString();
    }

    private static String resolveClassNameFromRefine(RefineAs refine) {
        try {
            return refine.value().getName();
        } catch (MirroredTypeException e) {
            return resolveInternalName(((DeclaredType) e.getTypeMirror()).asElement());
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(RefineAs.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            for (final Element element : roundEnv.getElementsAnnotatedWith(RefineAs.class)) {
                if (!(element instanceof TypeElement)) {
                    continue;
                }

                final RefineAs refine = element.getAnnotation(RefineAs.class);
                if (refine == null) {
                    continue;
                }

                final String original = resolveInternalName(element);
                final String replaced = resolveClassNameFromRefine(refine);

                processType((TypeElement) element, element, original, replaced);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    private void processType(TypeElement type, Element root, String prefix, String replace) throws IOException {
        final String sourceInternalName = resolveInternalName(type);

        System.out.println(sourceInternalName);

        if (!sourceInternalName.startsWith(prefix)) {
            return;
        }

        final String[] sourceFragments = sourceInternalName.split("/");
        final String sourcePackageName = Stream.of(sourceFragments)
                .limit(sourceFragments.length - 1)
                .collect(Collectors.joining("."));
        final String sourceClassName = sourceFragments[sourceFragments.length - 1];

        final FileObject refineFile = processingEnv.getFiler()
                .createResource(StandardLocation.CLASS_OUTPUT, sourcePackageName, sourceClassName + REFINE_FILE_SUFFIX, root);

        final String targetInternalName = replace + sourceInternalName.substring(prefix.length());
        try (Writer writer = refineFile.openWriter()) {
            writer.write(targetInternalName);
        }

        final String[] targetFragments = targetInternalName.split("/");
        final String targetPackageName = Stream.of(targetFragments)
                .limit(sourceFragments.length - 1)
                .collect(Collectors.joining("."));
        final String targetClassName = targetFragments[targetFragments.length - 1];

        final TypeElement target = processingEnv.getElementUtils()
                .getTypeElement(targetPackageName + "." + targetClassName.replace('$', '.'));
        if (target == null) {
            final FileObject classFile = processingEnv.getFiler()
                    .createResource(StandardLocation.CLASS_OUTPUT, targetPackageName, targetClassName + ".class", root);

            try (DataOutputStream stream = new DataOutputStream(classFile.openOutputStream())) {
                new ClassFile(false, targetFragments[targetFragments.length - 1], null).write(stream);
            }
        }

        for (final Element enclosed : type.getEnclosedElements()) {
            if (!(enclosed instanceof TypeElement)) {
                continue;
            }

            if (enclosed.getAnnotation(RefineAs.class) != null) {
                continue;
            }

            processType((TypeElement) enclosed, root, prefix, replace);
        }
    }
}
