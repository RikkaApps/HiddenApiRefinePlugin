package dev.rikka.tools.refine;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.MemberValue;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Map;

public final class RefineCollector {
    public static Map.Entry<String, String> collect(InputStream stream) throws IOException {
        final DataInputStream in = new DataInputStream(new BufferedInputStream(stream));
        final ClassFile file = new ClassFile(in);

        for (AttributeInfo info : file.getAttributes()) {
            if (info instanceof AnnotationsAttribute) {
                final Annotation annotation = ((AnnotationsAttribute) info).getAnnotation(RefineProcessor.DESCRIPTOR_REFINE_DESCRIPTOR);
                if (annotation == null)
                    continue;

                final MemberValue from = annotation.getMemberValue(RefineProcessor.DESCRIPTOR_REFINE_FROM);
                final MemberValue to = annotation.getMemberValue(RefineProcessor.DESCRIPTOR_REFINE_TO);
                return new AbstractMap.SimpleEntry<>(
                        ((ClassMemberValue) from).getValue().replace('.', '/'),
                        ((ClassMemberValue) to).getValue().replace('.', '/')
                );
            }
        }

        return null;
    }
}
