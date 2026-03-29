package implementations.java.compression.src.utils.image.pixel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class GrayPixel extends Pixel {
    int level;
    boolean asAscii = true;

    public GrayPixel(int level) {
        if (level < 0 || level > 255) {
            throw new IllegalArgumentException("Level value must be between 0 and 255.");
        }
        this.level = level;
    }

    @Override
    public void addToFile(FileOutputStream fos) throws IOException {

        fos.write(Integer.toString(level).getBytes(StandardCharsets.US_ASCII));

    }

    @Override
    public String toString() {
        return String.format("%d", level);
    }

    public int gray() {
        return level;
    }

    @Override
    public double sqDiff(Pixel other) {
        if (other instanceof GrayPixel grayPixel) {
            return Math.pow(this.level - grayPixel.level, 2);
        }
        return 0;
    }

}
