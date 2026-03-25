package implementations.java.compression.src.utils.ppm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import implementations.java.compression.src.utils.image.Pixel;

public class PPMUtils {

    public static void saveToImage(Pixel[][] pixels, String path) throws FileNotFoundException, IOException {
        int rows = pixels.length;
        int cols = pixels[0].length;
        PPMImageMetadata metadata = new PPMImageMetadata(rows, cols, 255, pixels);
        metadata.saveToFile(path);
    }

    public static double calculateMSE(String originalImgPath, String lastIterationPath) throws IOException {

        FileInputStream ofis = new FileInputStream(originalImgPath);
        FileInputStream ffis = new FileInputStream(lastIterationPath);

        PPMImageMetadata originalImg = new PPMImageMetadata(ofis);
        PPMImageMetadata finalImg = new PPMImageMetadata(ffis);

        int width = originalImg.getWidth();
        int height = originalImg.getHeight();

        Pixel[][] originalPixels = originalImg.getPixels();
        Pixel[][] finaPixels = finalImg.getPixels();

        double totalError = 0;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Pixel originalPixel = originalPixels[i][j];
                Pixel finalPixel = finaPixels[i][j];

                totalError += originalPixel.sqDiff(finalPixel);
            }
        }

        return totalError / (width * height);

    }

}
