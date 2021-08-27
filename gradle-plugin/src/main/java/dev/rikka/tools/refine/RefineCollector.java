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
    private static final String ANNOTATION_REFINE_AS_DESCRIPTOR = RefineAs.class.getName().replace('.', '/');
    private static final String ANNOTATION_DEFAULT_VALUE = "value";

    public static Map.Entry<String, String> collect(InputStream stream) throws IOException {
        final DataInputStream in = new DataInputStream(new BufferedInputStream(stream));
        final ClassFile file = new ClassFile(in);

        for (AttributeInfo info : file.getAttributes()) {
            if (info instanceof AnnotationsAttribute) {
                final Annotation annotation = ((AnnotationsAttribute) info).getAnnotation(ANNOTATION_REFINE_AS_DESCRIPTOR);
                if (annotation == null)
                    continue;

                final MemberValue value = annotation.getMemberValue(ANNOTATION_DEFAULT_VALUE);
                if (value instanceof ClassMemberValue) {
                    return new AbstractMap.SimpleEntry<>(file.getName(), ((ClassMemberValue) value).getValue());
                }
            }
        }

        return null;
    }
}
