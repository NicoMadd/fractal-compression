package implementations.java.compression.src.utils.image.pgm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import implementations.java.compression.src.utils.image.pixel.GrayPixel;;

public class PGMAUtils {

    public static void saveToImage(GrayPixel[][] pixels, String path) throws FileNotFoundException, IOException {
        int rows = pixels.length;
        int cols = pixels[0].length;
        PGMAImageMetadata metadata = new PGMAImageMetadata(rows, cols, 255, pixels);
        String finalPath = path + ".pgm";
        metadata.saveToFile(finalPath);
    }

    public static double calculateMSE(String originalImgPath, String lastIterationPath) throws IOException {

        FileInputStream ofis = new FileInputStream(originalImgPath);
        FileInputStream ffis = new FileInputStream(lastIterationPath);

        PGMAImageMetadata originalImg = new PGMAImageMetadata(ofis);
        PGMAImageMetadata finalImg = new PGMAImageMetadata(ffis);

        int width = originalImg.getWidth();
        int height = originalImg.getHeight();

        GrayPixel[][] originalPixels = originalImg.getPixels();
        GrayPixel[][] finaPixels = finalImg.getPixels();

        double totalError = 0;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                GrayPixel originalPixel = originalPixels[i][j];
                GrayPixel finalPixel = finaPixels[i][j];

                totalError += originalPixel.sqDiff(finalPixel);
            }
        }

        return totalError / (width * height);

    }

}
