package implementations.java.compression.src.utils.errors;

public class ErrorUtils {

    public static double calculatePSNR(double mse) {
        if (mse == 0)
            return Double.POSITIVE_INFINITY;
        return 10 * Math.log10(Math.pow(255, 2) / mse);
    }

}
