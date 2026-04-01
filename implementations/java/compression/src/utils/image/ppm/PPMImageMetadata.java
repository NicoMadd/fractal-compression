package implementations.java.compression.src.utils.image.ppm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import implementations.java.compression.src.utils.files.readers.SequenceReader;
import implementations.java.compression.src.utils.image.ImageMetadata;
import implementations.java.compression.src.utils.image.pixel.BytePixel;
import implementations.java.compression.src.utils.image.pixel.IntPixel;
import implementations.java.compression.src.utils.image.pixel.MaxValType;
import implementations.java.compression.src.utils.image.pixel.RGBPixel;

/*
 * Class to hold the metadata of a PPM image.
 * width: width of the image
 * height: height of the image
 * maxVal: maximum value of the image
 * maxValType: type of the maximum value
 * pixels: pixels of the image
 * 
 * This was made following the PPM format specification: https://netpbm.sourceforge.net/doc/ppm.html
 */
public class PPMImageMetadata implements ImageMetadata<RGBPixel> {

    private int width;
    private int height;
    private int maxVal;
    private MaxValType maxValType;
    private RGBPixel[][] pixels;

    private static final int MAGIC_NUMBER_BYTES_SIZE = 2;
    private static final String MAGIC_NUMBER = "P6";

    public PPMImageMetadata(int width, int height, int maxVal, RGBPixel[][] pixels) {
        this.width = width;
        this.height = height;
        this.maxVal = maxVal;
        this.maxValType = validateMaxVal(maxVal);
        this.pixels = pixels;
    }

    public PPMImageMetadata(FileInputStream s) throws IOException {

        SequenceReader sr = new SequenceReader(s);

        // Read magic number
        byte[] magicNumber = sr.readNBytes(MAGIC_NUMBER_BYTES_SIZE);
        validateMagicNumber(magicNumber);
        sr.readWhitespace();

        skipComment(sr);

        this.width = sr.readNBytesAsInt(3);
        sr.readWhitespace();
        this.height = sr.readNBytesAsInt(3);
        sr.readWhitespace();
        this.maxVal = sr.readNBytesAsInt(3);
        this.maxValType = validateMaxVal(maxVal);

        sr.readWhitespace();

        this.pixels = new RGBPixel[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                pixels[i][j] = readPixel(sr);
            }
        }

    }

    private void validateMagicNumber(byte[] magicNumber) {
        String magicNumberRead = new String(magicNumber);
        if (!MAGIC_NUMBER.equals(magicNumberRead)) {
            throw new IllegalArgumentException(
                    "Magic number of image is not correct. Expected " + MAGIC_NUMBER + "but was: " + magicNumberRead);
        }
    }

    public RGBPixel[][] getPixels() {
        return pixels;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    private void skipComment(SequenceReader sr) throws IOException {
        if (sr.nextCharIs('#')) {
            sr.skipUntilLineBreak();
        }
    }

    private MaxValType validateMaxVal(int maxVal) {
        if (maxVal < 0 && maxVal > 65536) {
            throw new RuntimeException("maxVal detected is below 0 or higher than 65536");
        }

        return maxVal < 256 ? MaxValType.SINGLE : MaxValType.DOUBLE;
    }

    private RGBPixel readPixel(SequenceReader sr) throws IOException {
        if (maxValType.equals(MaxValType.SINGLE)) {
            byte red = sr.read();
            byte green = sr.read();
            byte blue = sr.read();
            return new BytePixel(red, green, blue);
        } else {
            int red = sr.readNBytesAsInt(2);
            int green = sr.readNBytesAsInt(2);
            int blue = sr.readNBytesAsInt(2);
            return new IntPixel(red, green, blue);
        }

    }

    public void saveToFile(String filename) throws FileNotFoundException, IOException {
        try (FileOutputStream fos = new FileOutputStream(filename)) {

            // Header
            fos.write(String.format("P6\n%d %d\n%d\n", width, height, maxVal).getBytes());

            // Pixels
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    RGBPixel p = pixels[row][col];
                    p.addToFile(fos);
                }
            }
        }
    }

}
