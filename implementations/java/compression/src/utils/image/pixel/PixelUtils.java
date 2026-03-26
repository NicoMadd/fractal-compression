package implementations.java.compression.src.utils.image.pixel;

import java.util.List;

public class PixelUtils {

    /**
     * @param pixels pixels to reduce to a single pixel
     * @return
     */
    public static Pixel reduce(List<Pixel> pixels) {
        return reduce(pixels.toArray(new Pixel[0]));

    }

    /**
     * @param pixels array of pixels to reduce
     * @return a single Pixel composed of the average of each color channel.
     */
    public static Pixel reduce(Pixel[] pixels) {
        int[] colorAvgs = new int[3];

        // For each color, sum the pixel value
        for (RGB c : RGB.values()) {
            int sum = 0;
            for (Pixel p : pixels) {
                sum += p.color(c);
            }

            colorAvgs[c.ordinal()] = sum / pixels.length;
        }

        return new IntPixel(colorAvgs[RGB.RED.ordinal()], colorAvgs[RGB.GREEN.ordinal()],
                colorAvgs[RGB.BLUE.ordinal()]);

    }

    public static Pixel getRandomPixel() {

        Double red = Math.random() * 255;
        Double green = Math.random() * 255;
        Double blue = Math.random() * 255;

        return new IntPixel(red.intValue(), green.intValue(), blue.intValue());
    }

}
