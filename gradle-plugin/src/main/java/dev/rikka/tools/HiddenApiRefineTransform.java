package dev.rikka.tools;

import com.android.build.api.transform.*;
import com.android.build.api.transform.QualifiedContent.Scope;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import dev.rikka.tools.refine.RefineClass;
import dev.rikka.tools.refine.RefineApplier;
import dev.rikka.tools.refine.RefineCache;
import dev.rikka.tools.refine.RefineCollector;
import dev.rikka.tools.utils.FileUtils;
import dev.rikka.tools.utils.JarUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

@SuppressWarnings({"deprecation", "ResultOfMethodCallIgnored"})
public class HiddenApiRefineTransform extends Transform {
    @Override
    public String getName() {
        return "HiddenApiRefine";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return Collections.singleton(QualifiedContent.DefaultContentType.CLASSES);
    }

    @Override
    public Set<? super Scope> getScopes() {
        return Set.of(Scope.PROJECT, Scope.SUB_PROJECTS, Scope.EXTERNAL_LIBRARIES);
    }

    @Override
    public Set<? super Scope> getReferencedScopes() {
        return Set.of(Scope.PROVIDED_ONLY);
    }

    @Override
    public boolean isIncremental() {
        return true;
    }

    @Override
    public boolean isCacheable() {
        return true;
    }

    private void doTransform(TransformInvocation transform) throws IOException {
        final HashMap<String, RefineClass> refines = new HashMap<>();
        final Gson gson = new Gson();

        boolean forceApply = !transform.isIncremental();

        // collect annotation defines
        if (!transform.isIncremental()) {
            FileUtils.deleteDirectory(transform.getContext().getTemporaryDir());
        }

        for (TransformInput input : transform.getReferencedInputs()) {
            for (JarInput jarInput : input.getJarInputs()) {
                final File inputFile = jarInput.getFile();
                final File cacheFile = Paths.get(
                        transform.getContext().getTemporaryDir().getAbsolutePath(),
                        ((Scope) jarInput.getScopes().toArray()[0]).name(),
                        jarInput.getName(),
                        "refine-cache.json"
                ).toFile();

                if (jarInput.getStatus() == Status.REMOVED) {
                    FileUtils.deleteDirectory(cacheFile.getParentFile());
                }

                if (jarInput.getStatus() == Status.NOTCHANGED && cacheFile.exists()) {
                    try {
                        RefineCache cache = gson.fromJson(new FileReader(cacheFile), RefineCache.class);
                        cache.getRefines().forEach(r -> refines.put(r.getOriginalClassName(), r));
                        continue;
                    } catch (Exception ignore) {
                        // ignore
                    }
                }

                final List<RefineClass> scopedRefines = new ArrayList<>();

                JarUtils.visit(inputFile, (entry, stream) -> {
                    if (!entry.getName().endsWith(".class"))
                        return;

                    ClassReader reader = new ClassReader(stream);
                    RefineCollector collector = new RefineCollector();

                    reader.accept(collector, 0);

                    final RefineClass refine = collector.collect();
                    if (!refine.getReplacedClassName().isEmpty() || refine.getMemberReplacement().size() > 0) {
                        scopedRefines.add(collector.collect());
                    }
                });

                cacheFile.getParentFile().mkdirs();

                try (JsonWriter writer = gson.newJsonWriter(new FileWriter(cacheFile))) {
                    gson.toJson(new RefineCache(scopedRefines), RefineCache.class, writer);
                }
                scopedRefines.forEach(r -> refines.put(r.getOriginalClassName(), r));

                forceApply = true;
            }

            // TODO: handle DirectoryInput
        }

        // Apply refines
        if (forceApply) {
            transform.getOutputProvider().deleteAll();
        }

        for (TransformInput input : transform.getInputs()) {
            for (JarInput jarInput : input.getJarInputs()) {
                final File inputFile = jarInput.getFile();
                final File outputFile = transform.getOutputProvider()
                        .getContentLocation(
                                jarInput.getName(),
                                jarInput.getContentTypes(),
                                jarInput.getScopes(),
                                Format.JAR
                        );

                if (!forceApply && jarInput.getStatus() == Status.NOTCHANGED) {
                    continue;
                }

                if (jarInput.getStatus() == Status.REMOVED) {
                    outputFile.delete();
                }

                try (JarOutputStream output = new JarOutputStream(new FileOutputStream(outputFile))) {
                    JarUtils.visit(inputFile, (entry, stream) -> {
                        output.putNextEntry(new JarEntry(entry.getName()));

                        if (entry.getName().endsWith(".class")) {
                            ClassReader reader = new ClassReader(stream);
                            ClassWriter writer = new ClassWriter(0);
                            RefineApplier applier = new RefineApplier(refines, writer);

                            reader.accept(applier, 0);

                            output.write(writer.toByteArray());
                        } else {
                            stream.transferTo(output);
                        }
                    });
                }
            }

            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                final Path root = directoryInput.getFile().toPath();
                final HashSet<String> changedFile = new HashSet<>();
                final File outputDir = transform.getOutputProvider()
                        .getContentLocation(
                                directoryInput.getName(),
                                directoryInput.getContentTypes(),
                                directoryInput.getScopes(),
                                Format.DIRECTORY
                        );

                if (!forceApply) {
                    directoryInput.getChangedFiles().forEach((file, status) -> {
                        switch (status) {
                            case CHANGED:
                            case ADDED:
                            case REMOVED:
                                changedFile.add(root.relativize(file.toPath()).toString());
                        }
                    });
                } else {
                    Files.walk(root)
                            .filter((p) -> p.toFile().isFile())
                            .forEach((p) -> changedFile.add(root.relativize(p).toString()));
                }

                for (String path : changedFile) {
                    final File inputFile = root.resolve(path).toFile();
                    final File outputFile = new File(outputDir, path);

                    System.out.println("[" + getName() + "] Transforming " + path);

                    if (!inputFile.exists()) {
                        outputFile.delete();

                        continue;
                    }

                    outputFile.getParentFile().mkdirs();

                    try (FileInputStream in = new FileInputStream(inputFile); FileOutputStream out = new FileOutputStream(outputFile)) {
                        if (inputFile.getName().endsWith(".class")) {
                            ClassReader reader = new ClassReader(in);
                            ClassWriter writer = new ClassWriter(0);

                            reader.accept(new RefineApplier(refines, writer), 0);

                            out.write(writer.toByteArray());
                        } else {
                            in.transferTo(out);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws IOException {
        try {
            doTransform(transformInvocation);
        } catch (Exception e) {
            e.printStackTrace();

            throw e;
        }
    }
}
