package implementations.java.compression.src.pipelines;

/**
 * Holds the latest iteration's error metrics after each decompression step.
 */
public class IterationOutMetrics {

    private double mse;
    private double mae;
    private double psnr;

    public void setMse(double mse) {
        this.mse = mse;
    }

    public void setMae(double mae) {
        this.mae = mae;
    }

    public void setPsnr(double psnr) {
        this.psnr = psnr;
    }

    public double getMse() {
        return mse;
    }

    public double getMae() {
        return mae;
    }

    public double getPsnr() {
        return psnr;
    }
}
