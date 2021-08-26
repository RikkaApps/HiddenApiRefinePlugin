package dev.rikka.tools.refine;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;

public class RefineApplier extends ClassVisitor {
    private final Map<String, Refine> refines;

    public RefineApplier(Map<String, Refine> refines, ClassVisitor parent) {
        super(Opcodes.ASM9, parent);

        this.refines = refines;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        final Refine replacedSuperClass = refines.get(superName);
        if (replacedSuperClass != null && replacedSuperClass.getReplacedClassName() != null) {
            superName = replacedSuperClass.getReplacedClassName();
        }

        for (int i = 0; i < interfaces.length; i++) {
            final Refine replacedClass = refines.get(interfaces[i]);
            if (replacedClass != null && replacedClass.getReplacedClassName() != null) {
                interfaces[i] = replacedClass.getReplacedClassName();
            }
        }

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                final Refine refine = refines.get(owner);
                if (refine != null) {
                    String replacedName = refine.getMemberReplacement()
                            .get(String.format(RefineCollector.FORMAT_METHOD_KEY, name, descriptor));
                    if (replacedName != null)
                        name = replacedName;
                    if (refine.getReplacedClassName() != null)
                        owner = refine.getReplacedClassName();
                }

                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                final Refine refine = refines.get(owner);
                if (refine != null) {
                    final String replacedName = refine.getMemberReplacement()
                            .get(String.format(RefineCollector.FORMAT_FIELD_KEY, name, descriptor));
                    if (replacedName != null)
                        name = replacedName;
                    if (refine.getReplacedClassName() != null)
                        owner = refine.getReplacedClassName();
                }

                super.visitFieldInsn(opcode, owner, name, descriptor);
            }
        };
    }
}
