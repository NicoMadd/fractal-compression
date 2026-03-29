package implementations.java.compression.src.utils.image;

import implementations.java.compression.src.utils.image.pixel.Pixel;

public interface ImageMetadata<T extends Pixel> {
    public T[][] getPixels();

    public int getWidth();

    public int getHeight();

}
