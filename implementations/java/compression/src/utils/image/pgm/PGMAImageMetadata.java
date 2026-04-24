package implementations.java.compression.src.utils.image.pgm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import implementations.java.compression.src.utils.files.readers.SequenceReader;
import implementations.java.compression.src.utils.image.ImageMetadata;
import implementations.java.compression.src.utils.image.pixel.GrayPixel;

public class PGMAImageMetadata implements ImageMetadata<GrayPixel> {

    private static final byte[] SPACE_BYTES = " ".getBytes();

    private int width;
    private int height;
    private int maxVal;
    private GrayPixel[][] pixels;

    private static final int MAGIC_NUMBER_BYTES_SIZE = 2;
    private static final String MAGIC_NUMBER = "P2";

    public PGMAImageMetadata(int width, int height, int maxVal, GrayPixel[][] pixels) {
        this.width = width;
        this.height = height;
        this.maxVal = maxVal;
        this.pixels = pixels;
    }

    public PGMAImageMetadata(FileInputStream s) throws IOException {

        SequenceReader sr = new SequenceReader(s);

        // Read magic number
        byte[] magicNumber = sr.readNBytes(MAGIC_NUMBER_BYTES_SIZE);
        validateMagicNumber(magicNumber);
        sr.readWhitespace();

        skipComment(sr);

        this.width = sr.readNextInt();
        this.height = sr.readNextInt();
        this.maxVal = sr.readNextInt();

        sr.readWhitespace();

        this.pixels = new GrayPixel[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                pixels[i][j] = readPixel(sr);
            }
        }

    }

    public GrayPixel[][] getPixels() {
        return pixels;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    private void validateMagicNumber(byte[] magicNumber) {
        String magicNumberRead = new String(magicNumber);
        if (!MAGIC_NUMBER.equals(magicNumberRead)) {
            throw new IllegalArgumentException(
                    "Magic number of image is not correct. Expected " + MAGIC_NUMBER + "but was: " + magicNumberRead);
        }
    }

    private void skipComment(SequenceReader sr) throws IOException {
        if (sr.nextCharIs('#')) {
            sr.skipUntilLineBreak();
        }
    }

    private GrayPixel readPixel(SequenceReader sr) throws IOException {

        int level = sr.readNextInt();

        return new GrayPixel(level);

    }

    public void saveToFile(String filename) throws FileNotFoundException, IOException {
        try (FileOutputStream fos = new FileOutputStream(filename)) {

            // Header
            fos.write(String.format(MAGIC_NUMBER + "\n%d  %d\n%d\n", width, height, maxVal).getBytes());

            // Pixels
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    GrayPixel p = pixels[row][col];
                    p.addToFile(fos);
                    fos.write(SPACE_BYTES);
                }
            }
        }
    }

}
