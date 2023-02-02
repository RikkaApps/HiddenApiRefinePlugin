package dev.rikka.tools.refine;

import com.google.auto.service.AutoService;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Annotation processor to generate metadata store classes.
 */
@AutoService(Processor.class)
public class RefineProcessor extends AbstractProcessor {
    /**
     * Class name to store metadata.
     */
    public static final String REFINE_METADATA_CLASS_NAME = String.valueOf(
            'R' << 16 | 'E' << 16 | 'F' << 8 | 'I' << 8 | 'N' | 'E'
    );

    /**
     * Refine metadata key 'to'
     */
    public static final String REFINE_NS_PACKAGE = "refine";

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(RefineAs.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    private void writeRefineMetadata(final String from, final String to, final Element... dependencies) throws IOException {
        final String metadataName = from + "$" + REFINE_METADATA_CLASS_NAME;
        final String refineToAnnotation = REFINE_NS_PACKAGE + "." + to;

        final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        writer.visit(
                Opcodes.V1_8,
                Opcodes.ACC_FINAL | Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER,
                metadataName.replace('.', '/'),
                null,
                Type.getInternalName(Object.class),
                null
        );
        writer.visitAnnotation(Type.getDescriptor(Descriptor.class), false).visitEnd();
        writer.visitAnnotation("L" + refineToAnnotation.replace('.', '/') + ";", false).visitEnd();
        writer.visitEnd();

        final List<String> fromSegments = Arrays.asList(metadataName.split("\\."));
        final FileObject classFile = processingEnv.getFiler().createResource(
                StandardLocation.CLASS_OUTPUT,
                String.join(".", fromSegments.subList(0, fromSegments.size() - 1)),
                fromSegments.get(fromSegments.size() - 1) + ".class",
                dependencies
        );

        try (final OutputStream stream = classFile.openOutputStream()) {
            stream.write(writer.toByteArray());
        }
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        try {
            final TypeElement eRefineAs = processingEnv.getElementUtils().getTypeElement(RefineAs.class.getName());
            final ExecutableElement eRefineAsValue = (ExecutableElement) eRefineAs.getEnclosedElements().stream()
                    .filter(e -> (e instanceof ExecutableElement) && e.getSimpleName().contentEquals("value"))
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException("Invalid @RefineAs annotation."));

            for (final Element element : roundEnv.getElementsAnnotatedWith(eRefineAs)) {
                if (!(element instanceof TypeElement)) {
                    continue;
                }

                final AnnotationMirror refineAs = element.getAnnotationMirrors().stream()
                        .filter(a -> eRefineAs.equals(a.getAnnotationType().asElement()))
                        .findAny()
                        .orElse(null);
                if (refineAs == null) {
                    continue;
                }

                final AnnotationValue refineAsValue = refineAs.getElementValues().get(eRefineAsValue);
                if (refineAsValue == null) {
                    continue;
                }

                final String fromClass = ((TypeElement) element).getQualifiedName().toString();
                final String toClass = refineAsValue.getValue().toString();

                writeRefineMetadata(fromClass, toClass, element);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    /**
     * Mark class as refine metadata classes.
     */
    public @interface Descriptor {
    }
}
