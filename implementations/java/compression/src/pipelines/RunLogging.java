package implementations.java.compression.src.pipelines;

/** Gate for verbose stdout during a pipeline run; set from {@link PipelineParams#debug()}. */
public final class RunLogging {

    private static boolean debug;

    private RunLogging() {
    }

    public static void setDebug(boolean debug) {
        RunLogging.debug = debug;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void debug(String message) {
        if (debug) {
            System.out.println(message);
        }
    }
}
