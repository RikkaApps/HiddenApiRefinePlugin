package dev.rikka.tools.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@SuppressWarnings("ResultOfMethodCallIgnored")
public final class FileUtils {
    public static void deleteDirectory(File directory) throws IOException {
        Files.walk(directory.getAbsoluteFile().toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
