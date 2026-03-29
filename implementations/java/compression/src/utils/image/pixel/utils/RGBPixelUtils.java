package implementations.java.compression.src.utils.image.pixel.utils;

import java.util.List;

import implementations.java.compression.src.utils.image.pixel.IntPixel;
import implementations.java.compression.src.utils.image.pixel.RGB;
import implementations.java.compression.src.utils.image.pixel.RGBPixel;

public class RGBPixelUtils {

    /**
     * @param pixels pixels to reduce to a single pixel
     * @return
     */
    public static RGBPixel reduce(List<RGBPixel> pixels) {
        return reduce(pixels.toArray(new RGBPixel[0]));
    }

    /**
     * @param pixels array of pixels to reduce
     * @return a single Pixel composed of the average of each color channel.
     */
    public static RGBPixel reduce(RGBPixel[] pixels) {
        int[] colorAvgs = new int[3];

        // For each color, sum the pixel value
        for (RGB c : RGB.values()) {
            int sum = 0;
            for (RGBPixel p : pixels) {
                sum += p.color(c);
            }

            colorAvgs[c.ordinal()] = sum / pixels.length;
        }

        return new IntPixel(colorAvgs[RGB.RED.ordinal()], colorAvgs[RGB.GREEN.ordinal()],
                colorAvgs[RGB.BLUE.ordinal()]);

    }

    public static RGBPixel getRandomPixel() {

        Double red = Math.random() * 255;
        Double green = Math.random() * 255;
        Double blue = Math.random() * 255;

        return new IntPixel(red.intValue(), green.intValue(), blue.intValue());
    }

}
