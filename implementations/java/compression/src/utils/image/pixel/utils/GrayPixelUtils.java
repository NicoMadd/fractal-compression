package implementations.java.compression.src.utils.image.pixel.utils;

import implementations.java.compression.src.utils.image.pixel.GrayPixel;

public class GrayPixelUtils {

    public static float valueMapper(GrayPixel p) {
        return p.gray();
    }

    public static GrayPixel getRandomPixel() {
        Double gray = Math.random() * 255;
        return new GrayPixel(gray.intValue());
    }

}
