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

    /**
     * Root directory: {@code processes/} at repo level when cwd is the Java module.
     */
    public static final Path PROCESSES_ROOT = Paths.get("").toAbsolutePath().resolve("../../../processes");

    /**
     * Subdirectory under each image folder where iteration outputs (PGMs,
     * benchmark) are stored.
     */
    public static final String ITERATIONS_SUBDIR = "iterations";

    public static Path iterationsDir(Path imageProcessDir) {
        return imageProcessDir.resolve(ITERATIONS_SUBDIR);
    }

    public static Path inRunDir(Path parentDir, String basenameNoExt) {
        return parentDir.resolve(basenameNoExt);
    }

    /** Folder name derived from image filename (stem), safe for the filesystem. */
    public static String runFolderName(String imagePath) {
        String fileName = Paths.get(imagePath).getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String stem = dot > 0 ? fileName.substring(0, dot) : fileName;
        String safe = stem.replaceAll("[^a-zA-Z0-9._-]", "_");
        return safe.isEmpty() ? "image" : safe;
    }

    /**
     * Deletes and recreates only {@code <imageProcessDir>/iterations/}, preserving
     * {@code codebook.fc}, {@code original.pgm}, and other files in the image
     * folder.
     */
    public static void cleanAndCreateIterationsDir(Path imageProcessDir) throws IOException {
        Path iterDir = iterationsDir(imageProcessDir);
        if (Files.isDirectory(iterDir)) {
            try (Stream<Path> walk = Files.walk(iterDir)) {
                walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            }
        }
        Files.createDirectories(iterDir);
    }

    public static void copyOriginal(String imagePath, Path runDir) throws IOException {
        Path dest = runDir.resolve("original.pgm");
        Files.copy(Paths.get(imagePath), dest, StandardCopyOption.REPLACE_EXISTING);
    }

    public static long getFileSize(String filepath) throws IOException {
        return Files.size(Path.of(filepath));
    }

}
