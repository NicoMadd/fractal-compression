package implementations.java.compression.src.benchmarks;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public record RunManifest(
        long epochMs,
        String gitSha,
        String sourceImagePath,
        String runDirPath,
        int iterations,
        int rangeSize,
        int domainSize,
        boolean cleanCodebook,
        boolean skipIterationSaves,
        double compressionSeconds,
        double decompressionSeconds,
        double decompressionDecodeSeconds,
        double decompressionSaveSeconds,
        double decompressionSnapshotMetricsSeconds,
        double decodeIterationAvgSeconds,
        double decodeIterationMinSeconds,
        double decodeIterationMaxSeconds,
        long bytesOriginalInput,
        long bytesOriginalPgmCopy,
        long bytesCodebook,
        long pngBytesOriginalPgm,
        long pngBytesFinalIter,
        long zipBytesOriginalPgm,
        long zipBytesCodebook,
        double ratioOriginalOverCodebook,
        double mseFinal,
        double maeFinal,
        double psnrFinal,
        long peakHeapUsedBytes,
        long peakRuntimeUsedBytes,
        long avgHeapUsedBytes,
        long avgRuntimeUsedBytes,
        RunnerInfo runner,
        HostEnvironmentMetrics host) {

    /** Bump only for breaking JSON renames; see {@code benchmarks/RUN_RECORD.md}. */
    public static final int SCHEMA_VERSION = 1;

    public static String gitShaFromEnv() {
        String s = System.getenv("BENCHMARK_GIT_SHA");
        return s == null ? "" : s;
    }

    private static String jStr(String s) {
        if (s == null) {
            return "null";
        }
        String escaped = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
        return "\"" + escaped + "\"";
    }

    private static String jDbl(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return "null";
        }
        return String.format(Locale.US, "%.12g", v);
    }

    public String toJsonLine() {
        return "{"
                + "\"schema_version\":" + SCHEMA_VERSION + ","
                + "\"runner\":{" + runner.toJsonObjectInner() + "},"
                + "\"epoch_ms\":" + epochMs + ","
                + "\"git_sha\":" + jStr(gitSha) + ","
                + "\"source_image\":" + jStr(sourceImagePath) + ","
                + "\"run_dir\":" + jStr(runDirPath) + ","
                + "\"iterations\":" + iterations + ","
                + "\"range_size\":" + rangeSize + ","
                + "\"domain_size\":" + domainSize + ","
                + "\"clean_codebook\":" + cleanCodebook + ","
                + "\"skip_iteration_saves\":" + skipIterationSaves + ","
                + "\"compression_seconds\":" + jDbl(compressionSeconds) + ","
                + "\"decompression_seconds\":" + jDbl(decompressionSeconds) + ","
                + "\"decompression_decode_seconds\":" + jDbl(decompressionDecodeSeconds) + ","
                + "\"decompression_save_seconds\":" + jDbl(decompressionSaveSeconds) + ","
                + "\"decompression_snapshot_metrics_seconds\":" + jDbl(decompressionSnapshotMetricsSeconds) + ","
                + "\"decode_iteration_avg_seconds\":" + jDbl(decodeIterationAvgSeconds) + ","
                + "\"decode_iteration_min_seconds\":" + jDbl(decodeIterationMinSeconds) + ","
                + "\"decode_iteration_max_seconds\":" + jDbl(decodeIterationMaxSeconds) + ","
                + "\"bytes_original_input\":" + bytesOriginalInput + ","
                + "\"bytes_original_pgm_copy\":" + bytesOriginalPgmCopy + ","
                + "\"bytes_codebook\":" + bytesCodebook + ","
                + "\"png_bytes_original_pgm\":" + pngBytesOriginalPgm + ","
                + "\"png_bytes_final_iter\":" + pngBytesFinalIter + ","
                + "\"zip_bytes_original_pgm\":" + zipBytesOriginalPgm + ","
                + "\"zip_bytes_codebook\":" + zipBytesCodebook + ","
                + "\"ratio_original_over_codebook\":" + jDbl(ratioOriginalOverCodebook) + ","
                + "\"mse_final\":" + jDbl(mseFinal) + ","
                + "\"mae_final\":" + jDbl(maeFinal) + ","
                + "\"psnr_final\":" + jDbl(psnrFinal) + ","
                + "\"peak_heap_used_bytes\":" + peakHeapUsedBytes + ","
                + "\"peak_runtime_used_bytes\":" + peakRuntimeUsedBytes + ","
                + "\"avg_heap_used_bytes\":" + avgHeapUsedBytes + ","
                + "\"avg_runtime_used_bytes\":" + avgRuntimeUsedBytes + ","
                + "\"host\":{" + host.toJsonObjectInner() + "}"
                + "}";
    }

    public void write(Path runDir) throws IOException {
        Path p = runDir.resolve("run_manifest.json");
        Files.writeString(p, toJsonLine() + "\n", StandardCharsets.UTF_8);
    }
}
