package dev.rikka.tools;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Status;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;

import javassist.bytecode.ClassFile;

public class HiddenApiRefineTransform extends Transform {
    private final Function<String, String> rename;

    public HiddenApiRefineTransform(Function<String, String> rename) {
        this.rename = rename;
    }

    @Override
    public String getName() {
        return "HiddenApiRefine";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return Collections.singleton(QualifiedContent.DefaultContentType.CLASSES);
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return new HashSet<>(
                Arrays.asList(
                        QualifiedContent.Scope.PROJECT,
                        QualifiedContent.Scope.SUB_PROJECTS,
                        QualifiedContent.Scope.EXTERNAL_LIBRARIES
                )
        );
    }

    @Override
    public boolean isIncremental() {
        return true;
    }

    private void doTransform(TransformInvocation transform) throws IOException {
        if (!transform.isIncremental())
            transform.getOutputProvider().deleteAll();

        for (final JarInput inputJar : collectChangedJars(transform)) {
            final File inputFile = inputJar.getFile();
            final File outputFile = transform.getOutputProvider()
                    .getContentLocation(
                            inputJar.getName(),
                            inputJar.getContentTypes(),
                            inputJar.getScopes(),
                            Format.JAR);

            if (inputFile == null || !inputFile.exists()) {
                outputFile.delete();

                continue;
            }

            try (final JarInputStream inputStream = new JarInputStream(new FileInputStream(inputFile));
                 final JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(outputFile))) {
                while (true) {
                    final JarEntry entry = inputStream.getNextJarEntry();

                    if (entry == null)
                        break;

                    if (entry.getName().endsWith(".class")) {
                        final ClassFile file = loadClass(new BufferedInputStream(inputStream));

                        patchClass(file);

                        outputStream.putNextEntry(new JarEntry(file.getName().replace('.', '/') + ".class"));

                        final BufferedOutputStream out = new BufferedOutputStream(outputStream);

                        file.write(new DataOutputStream(out));

                        out.flush();
                    } else {
                        outputStream.putNextEntry(new JarEntry(entry.getName()));

                        copy(inputStream, outputStream);
                    }
                }
            }
        }

        for (final TransformInput input : transform.getInputs()) {
            for (final DirectoryInput directory : input.getDirectoryInputs()) {
                for (final Map.Entry<File, String> entry : collectChangedFiles(directory, transform.isIncremental()).entrySet()) {
                    final File outputDir = transform.getOutputProvider().getContentLocation(
                            directory.getName(),
                            directory.getContentTypes(),
                            directory.getScopes(),
                            Format.DIRECTORY
                    );

                    if (!entry.getKey().getName().endsWith(".class")) {
                        final File outputFile = new File(outputDir, entry.getValue());

                        if (!entry.getKey().exists()) {
                            outputFile.delete();

                            continue;
                        }

                        outputFile.getParentFile().mkdirs();

                        try (final FileInputStream inputStream = new FileInputStream(entry.getKey())) {
                            try (final FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                                copy(inputStream, outputStream);
                            }
                        }

                        continue;
                    }

                    final String className = entry.getValue()
                            .substring(0, entry.getValue().length() - ".class".length())
                            .replace(File.separatorChar, '/');
                    final String renamed = rename.apply(className)
                            .replace('/', File.separatorChar) + ".class";

                    final File outputFile = new File(outputDir, renamed);

                    if (!entry.getKey().exists()) {
                        outputFile.delete();

                        continue;
                    }

                    final ClassFile file;

                    try (final FileInputStream stream = new FileInputStream(entry.getKey())) {
                        file = loadClass(new BufferedInputStream(stream));
                    }

                    patchClass(file);

                    outputFile.getParentFile().mkdirs();

                    try (final BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                        file.write(new DataOutputStream(stream));
                    }
                }
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void transform(TransformInvocation transform) throws IOException {
        try {
            doTransform(transform);
        } catch (IOException e) {
            e.printStackTrace();

            throw e;
        }
    }

    private List<JarInput> collectChangedJars(TransformInvocation transform) {
        if (!transform.isIncremental()) {
            return transform.getInputs().stream()
                    .flatMap((tran) -> tran.getJarInputs().stream())
                    .collect(Collectors.toList());
        } else {
            return transform.getInputs().stream()
                    .flatMap((tran) -> tran.getJarInputs().stream())
                    .filter((jar) -> jar.getStatus() != Status.NOTCHANGED)
                    .collect(Collectors.toList());
        }
    }

    private Map<File, String> collectChangedFiles(
            DirectoryInput directory,
            boolean isIncremental
    ) throws IOException {
        final Path root = directory.getFile().toPath();
        final HashMap<File, String> result = new HashMap<>();

        if (!isIncremental) {
            Files.walk(root)
                    .filter((p) -> p.toFile().isFile())
                    .forEach((p) -> {
                        final String path = root.relativize(p).toString();

                        result.put(new File(root.toAbsolutePath().toFile(), path), path);
                    });
        } else {
            directory.getChangedFiles().entrySet().stream()
                    .filter((s) -> s.getKey().isFile() || s.getValue() == Status.REMOVED)
                    .filter((s) -> s.getValue() != Status.NOTCHANGED)
                    .forEach((s) -> {
                        final String path = root.relativize(s.getKey().toPath()).toString();

                        result.put(new File(root.toAbsolutePath().toFile(), path), path);
                    });
        }

        return result;
    }

    private ClassFile loadClass(BufferedInputStream stream) throws IOException {
        return new ClassFile(new DataInputStream(stream));
    }

    private void patchClass(ClassFile file) {
        final HashMap<String, String> replacedName = new HashMap<>();

        file.getConstPool().getClassNames().forEach((name) -> {
            replacedName.put(name, rename.apply(name));
        });

        file.renameClass(replacedName);

        file.setName(rename.apply(file.getName()));
    }

    private void copy(InputStream input, OutputStream output) throws IOException {
        final byte[] buffer = new byte[4096];
        int size;

        while ((size = input.read(buffer)) > 0) {
            output.write(buffer, 0, size);
        }
    }
}
