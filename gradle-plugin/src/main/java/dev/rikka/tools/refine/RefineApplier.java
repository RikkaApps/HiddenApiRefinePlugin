package dev.rikka.tools.refine;

import javassist.bytecode.ClassFile;

import java.io.*;
import java.util.Map;

public final class RefineApplier {
    private final Map<String, String> refines;

    public RefineApplier(Map<String, String> refines) {
        this.refines = refines;
    }

    public void applyFor(InputStream in, OutputStream out) throws IOException {
        final DataInputStream input = new DataInputStream(new BufferedInputStream(in));
        final DataOutputStream output = new DataOutputStream(new BufferedOutputStream(out));
        final ClassFile file = new ClassFile(input);
        final String selfReplaced = refines.remove(file.getName());

        try {
            file.renameClass(refines);
        } finally {
            if (selfReplaced != null) {
                refines.put(file.getName(), selfReplaced);
            }
        }

        file.write(output);
        output.flush();
    }
}
