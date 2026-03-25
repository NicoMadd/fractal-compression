package implementations.java.compression.src.benchmarks;

import java.util.Locale;

public record Iteration(int n, long timestamp, long duration, double mse, double psnr) {

    public static String columns() {
        return "n,timestamp,durationNanos,durationSeconds,mse,psnr";
    }

    public String toString() {
        double compressionSeconds = duration / 1_000_000_000.0;

        StringBuilder sb = new StringBuilder();
        sb.append(n);
        sb.append(',');
        sb.append(timestamp);
        sb.append(',');
        sb.append(duration);
        sb.append(',');
        sb.append(compressionSeconds);
        sb.append(',');
        sb.append(String.format(Locale.US, "%.12f", mse));
        sb.append(',');
        sb.append(String.format(Locale.US, "%.12f", psnr));
        return sb.toString();

    }

}
