package implementations.java.compression.src.utils.image.ppm;

import java.io.FileOutputStream;
import java.io.IOException;

import implementations.java.compression.src.utils.image.Pixel;

public class BytePixel extends Pixel {
    byte red;
    byte green;
    byte blue;

    public BytePixel(byte red, byte green, byte blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public void addToFile(FileOutputStream fos) throws IOException {
        fos.write(red);
        fos.write(green);
        fos.write(blue);
    }

    @Override
    public String toString() {
        return String.format("(%d, %d, %d)", red, green, blue);
    }

    public int red() {
        return red;
    }

    public int green() {
        return green;
    }

    public int blue() {
        return blue;
    }
}
