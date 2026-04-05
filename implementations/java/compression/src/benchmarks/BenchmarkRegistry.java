package implementations.java.compression.src.benchmarks;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import implementations.java.compression.src.utils.files.FileUtils;

public final class BenchmarkRegistry {

    private BenchmarkRegistry() {
    }

    public static void appendLine(String jsonLine) throws IOException {
        Path path = FileUtils.benchmarkRegistryPath();
        Files.createDirectories(path.getParent());
        Files.writeString(path, jsonLine + "\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }
}
