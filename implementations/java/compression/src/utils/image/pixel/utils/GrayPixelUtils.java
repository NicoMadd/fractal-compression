package implementations.java.compression.src.utils.image.pixel.utils;

import java.util.List;

import implementations.java.compression.src.utils.image.pixel.GrayPixel;

public class GrayPixelUtils {

    /**
     * @param pixels pixels to reduce to a single pixel
     * @return
     */
    public static GrayPixel reduce(List<GrayPixel> pixels) {
        return reduce(pixels.toArray(new GrayPixel[0]));
    }

    /**
     * @param pixels array of pixels to reduce
     * @return a single Pixel composed of the average of each color channel.
     */
    public static GrayPixel reduce(GrayPixel[] pixels) {
        int colorAvg = 0;

        // For each color, sum the pixel value
        int sum = 0;
        for (GrayPixel p : pixels) {
            sum += p.gray();
        }

        colorAvg = sum / pixels.length;

        return new GrayPixel(colorAvg);

    }

    public static GrayPixel getRandomPixel() {
        Double gray = Math.random() * 255;
        return new GrayPixel(gray.intValue());
    }

}
