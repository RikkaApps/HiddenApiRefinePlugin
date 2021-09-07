package dev.rikka.tools.refine;

import com.android.build.api.transform.*;
import com.android.build.api.transform.QualifiedContent.Scope;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import dev.rikka.tools.refine.utils.FileUtils;
import dev.rikka.tools.refine.utils.JarUtils;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

@SuppressWarnings({"deprecation", "ResultOfMethodCallIgnored"})
public class RefineTransform extends Transform {
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
        final Logger logger = Logging.getLogger(RefineTransform.class);
        final HashMap<String, String> refines = new HashMap<>();
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
                        jarInput.getName(),
                        "refine-cache.json"
                ).toFile();

                if (jarInput.getStatus() == Status.REMOVED) {
                    FileUtils.deleteDirectory(cacheFile.getParentFile());
                    continue;
                }

                if (jarInput.getStatus() == Status.NOTCHANGED && cacheFile.exists()) {
                    try {
                        RefineCache cache = gson.fromJson(new FileReader(cacheFile), RefineCache.class);
                        refines.putAll(cache.getRefines());
                        logger.debug("Refines from " + jarInput.getFile() + " cache matched");
                        continue;
                    } catch (Exception ignore) {
                        // ignore
                    }
                }

                final HashMap<String, String> scopedRefines = new HashMap<>();

                JarUtils.visit(inputFile, (entry, stream) -> {
                    if (!entry.getName().endsWith(".class"))
                        return;

                    logger.debug("Collecting " + entry.getName());

                    final Map.Entry<String, String> refine = RefineCollector.collect(stream);
                    if (refine != null) {
                        scopedRefines.put(refine.getKey(), refine.getValue());
                    }
                });

                cacheFile.getParentFile().mkdirs();

                try (JsonWriter writer = gson.newJsonWriter(new FileWriter(cacheFile))) {
                    gson.toJson(new RefineCache(scopedRefines), RefineCache.class, writer);
                }

                refines.putAll(scopedRefines);

                forceApply = true;
            }

            // TODO: handle DirectoryInput
        }

        logger.info("Refines " + refines);

        // Apply refines
        if (forceApply) {
            transform.getOutputProvider().deleteAll();
        }

        final RefineApplier applier = new RefineApplier(refines);

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
                    continue;
                }

                try (JarOutputStream output = new JarOutputStream(new FileOutputStream(outputFile))) {
                    JarUtils.visit(inputFile, (entry, stream) -> {
                        output.putNextEntry(new JarEntry(entry.getName()));

                        if (entry.getName().endsWith(".class")) {
                            logger.debug("Transforming " + entry.getName());

                            applier.applyFor(stream, output);
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

                    if (!inputFile.exists()) {
                        outputFile.delete();

                        continue;
                    }

                    outputFile.getParentFile().mkdirs();

                    try (FileInputStream in = new FileInputStream(inputFile); FileOutputStream out = new FileOutputStream(outputFile)) {
                        if (inputFile.getName().endsWith(".class")) {
                            logger.debug("Transforming " + path.replace('\\', '/'));

                            applier.applyFor(in, out);
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
