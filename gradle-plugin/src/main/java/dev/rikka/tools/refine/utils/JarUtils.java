package dev.rikka.tools.refine.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public final class JarUtils {
    public interface Visitor {
        void visit(JarEntry entry, InputStream stream) throws IOException;
    }

    public static void visit(File file, Visitor visitor) throws IOException {
        try (JarInputStream stream = new JarInputStream(new FileInputStream(file))) {
            while (true) {
                final JarEntry entry = stream.getNextJarEntry();
                if (entry == null)
                    break;

                visitor.visit(entry, stream);
            }
        }
    }
}
