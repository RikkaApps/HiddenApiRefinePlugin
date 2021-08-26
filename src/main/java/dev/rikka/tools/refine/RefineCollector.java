package dev.rikka.tools.refine;

import org.objectweb.asm.*;

import java.util.HashMap;

public class RefineCollector extends ClassVisitor {
    public static final String FORMAT_METHOD_KEY = "%s#%s";
    public static final String FORMAT_FIELD_KEY = "%s#%s";

    private static final String ANNOTATION_REFINE_AS_DESCRIPTOR = Type.getDescriptor(RefineAs.class);
    private static final String ANNOTATION_REFINE_NAME_DESCRIPTOR = Type.getDescriptor(RefineName.class);
    private static final String ANNOTATION_DEFAULT_VALUE = "value";

    private String originalClassName;
    private String replacedClassName;
    private final HashMap<String, String> memberReplacement = new HashMap<>();

    public RefineCollector() {
        super(Opcodes.ASM9);
    }

    public Refine collect() {
        return new Refine(originalClassName, replacedClassName, memberReplacement);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        originalClassName = name;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (ANNOTATION_REFINE_AS_DESCRIPTOR.equals(descriptor)) {
            return new AnnotationVisitor(Opcodes.ASM9) {
                @Override
                public void visit(String name, Object value) {
                    if (ANNOTATION_DEFAULT_VALUE.equals(name) && value instanceof Type) {
                        replacedClassName = ((Type) value).getInternalName();
                    }
                }
            };
        }

        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String methodName, String methodDescriptor, String signature, String[] exceptions) {
        return new MethodVisitor(Opcodes.ASM9) {
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                if (ANNOTATION_REFINE_NAME_DESCRIPTOR.equals(descriptor)) {
                    return new AnnotationVisitor(Opcodes.ASM9) {
                        @Override
                        public void visit(String name, Object value) {
                            if (ANNOTATION_DEFAULT_VALUE.equals(name) && value instanceof String) {
                                memberReplacement.put(String.format(FORMAT_METHOD_KEY, methodName, methodDescriptor), (String) value);
                            }
                        }
                    };
                }

                return null;
            };
        };
    }
}
