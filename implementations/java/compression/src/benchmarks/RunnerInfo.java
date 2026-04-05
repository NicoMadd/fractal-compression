package implementations.java.compression.src.benchmarks;

/**
 * Identifies the benchmark producer for cross-language {@code registry.jsonl} rows.
 * Env: {@code BENCHMARK_RUNNER_ID}, {@code BENCHMARK_RUNNER_VERSION} (optional).
 */
public record RunnerInfo(String language, String id, String version) {

    public static final String DEFAULT_JAVA_ID = "java-compression";

    public static RunnerInfo javaDefault() {
        String id = System.getenv("BENCHMARK_RUNNER_ID");
        if (id == null || id.isBlank()) {
            id = DEFAULT_JAVA_ID;
        }
        String ver = System.getenv("BENCHMARK_RUNNER_VERSION");
        if (ver == null) {
            ver = "";
        }
        return new RunnerInfo("java", id, ver);
    }

    private static String jStr(String s) {
        if (s == null) {
            return "null";
        }
        String escaped = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
        return "\"" + escaped + "\"";
    }

    public String toJsonObjectInner() {
        return "\"language\":" + jStr(language) + ","
                + "\"id\":" + jStr(id) + ","
                + "\"version\":" + jStr(version);
    }
}
