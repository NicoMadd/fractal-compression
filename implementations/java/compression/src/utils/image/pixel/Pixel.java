package implementations.java.compression.src.utils.image.pixel;

import java.io.FileOutputStream;
import java.io.IOException;

public abstract class Pixel {
    public abstract void addToFile(FileOutputStream fos) throws IOException;

    public abstract double sqDiff(Pixel other);
}
