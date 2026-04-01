package implementations.java.compression.src.utils.files;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;

public class FileUtils {

    public static final Path ITERATIONS_ROOT = Paths.get("").toAbsolutePath().resolve("../../../iterations");

    public static Path inRunDir(Path runDir, String basenameNoExt) {
        return runDir.resolve(basenameNoExt);
    }

    /** Folder name derived from image filename (stem), safe for the filesystem. */
    public static String runFolderName(String imagePath) {
        String fileName = Paths.get(imagePath).getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String stem = dot > 0 ? fileName.substring(0, dot) : fileName;
        String safe = stem.replaceAll("[^a-zA-Z0-9._-]", "_");
        return safe.isEmpty() ? "image" : safe;
    }

    public static void cleanAndCreateRunDir(Path runDir) throws IOException {
        if (Files.isDirectory(runDir)) {
            try (Stream<Path> walk = Files.walk(runDir)) {
                walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            }
        }
        Files.createDirectories(runDir);
    }

    public static void copyOriginal(String imagePath, Path runDir) throws IOException {
        Path dest = runDir.resolve("original.pgm");
        Files.copy(Paths.get(imagePath), dest, StandardCopyOption.REPLACE_EXISTING);
    }

}
