package implementations.java.compression.src.utils.image.pgm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import implementations.java.compression.src.benchmarks.ImageErrorMetrics;
import implementations.java.compression.src.utils.image.pixel.GrayPixel;

public class PGMAUtils {

    public static void saveToImage(GrayPixel[][] pixels, String path) throws FileNotFoundException, IOException {
        int rows = pixels.length;
        int cols = pixels[0].length;
        PGMAImageMetadata metadata = new PGMAImageMetadata(rows, cols, 255, pixels);
        String finalPath = path + ".pgm";
        metadata.saveToFile(finalPath);
    }

    public static ImageErrorMetrics calculateErrorMetrics(String originalImgPath, String lastIterationPath)
            throws IOException {

        try (FileInputStream ofis = new FileInputStream(originalImgPath);
                FileInputStream ffis = new FileInputStream(lastIterationPath)) {

            PGMAImageMetadata originalImg = new PGMAImageMetadata(ofis);
            PGMAImageMetadata finalImg = new PGMAImageMetadata(ffis);

            int width = originalImg.getWidth();
            int height = originalImg.getHeight();

            GrayPixel[][] originalPixels = originalImg.getPixels();
            GrayPixel[][] finaPixels = finalImg.getPixels();

            double totalSq = 0;
            double totalAbs = 0;

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    GrayPixel originalPixel = originalPixels[i][j];
                    GrayPixel finalPixel = finaPixels[i][j];

                    totalSq += originalPixel.sqDiff(finalPixel);
                    totalAbs += Math.abs((double) originalPixel.gray() - (double) finalPixel.gray());
                }
            }

            double n = (double) width * (double) height;
            return new ImageErrorMetrics(totalSq / n, totalAbs / n);
        }
    }

    public static double calculateMSE(String originalImgPath, String lastIterationPath) throws IOException {
        return calculateErrorMetrics(originalImgPath, lastIterationPath).mse();
    }

    /**
     * Same index pairing as {@link #calculateErrorMetrics(String, String)} on loaded rasters:
     * {@code referencePixels[i][j]} vs {@code reconstructedPixels[i][j]} for {@code i} in {@code [0,width)},
     * {@code j} in {@code [0,height)} (matches pipeline {@code GrayPixel[width][height]} buffers).
     */
    public static ImageErrorMetrics calculateErrorMetricsSameLayout(GrayPixel[][] referencePixels,
            GrayPixel[][] reconstructedPixels,
            int width,
            int height) {
        double totalSq = 0;
        double totalAbs = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                GrayPixel originalPixel = referencePixels[i][j];
                GrayPixel finalPixel = reconstructedPixels[i][j];
                totalSq += originalPixel.sqDiff(finalPixel);
                totalAbs += Math.abs((double) originalPixel.gray() - (double) finalPixel.gray());
            }
        }
        double n = (double) width * (double) height;
        return new ImageErrorMetrics(totalSq / n, totalAbs / n);
    }

}
