package implementations.java.compression.src.utils.image.pixel.utils;

import implementations.java.compression.src.utils.image.pixel.GrayPixel;
import implementations.java.compression.src.utils.matrix.MatrixUtils;

public class GrayPixelUtils {

    static float[][] BICUBIC_DOWNSAMPLING_KERNEL_05 = {
            { 0.00390625f, -0.03515625f, -0.03515625f, 0.00390625f },
            { -0.03515625f, 0.31640625f, 0.31640625f, -0.03515625f },
            { -0.03515625f, 0.31640625f, 0.31640625f, -0.03515625f },
            { 0.00390625f, -0.03515625f, -0.03515625f, 0.00390625f }
    };

    /**
     * @param pixels array of pixels to reduce
     * @return a single Pixel composed of the average of each color channel.
     */
    public static GrayPixel meanReduction(GrayPixel[][] pixels, int mainOffsetI, int mainOffsetJ, int reduceRatio) {

        float[][] innerPixels = new float[reduceRatio][reduceRatio];

        for (int ri = 0; ri < reduceRatio; ri++) {
            for (int rj = 0; rj < reduceRatio; rj++) {
                innerPixels[ri][rj] = pixels[mainOffsetI + ri][mainOffsetJ + rj].gray();
            }
        }

        int avg = (int) MatrixUtils.dotProduct(innerPixels, MatrixUtils.avgKernel(reduceRatio));

        return new GrayPixel(avg);

    }

    public static GrayPixel bicubicReduction(GrayPixel[][] pixels, int mainOffsetI, int mainOffsetJ, int reduceRatio) {
        GrayPixel[][] neighbours = new GrayPixel[reduceRatio * 2][reduceRatio * 2];
        MatrixUtils.copySquare(pixels, neighbours, mainOffsetI - 1, mainOffsetJ - 1, reduceRatio * 2);

        float[][] grayValues = MatrixUtils.floatMap(neighbours, p -> (float) p.gray());

        float value = MatrixUtils.dotProduct(grayValues, BICUBIC_DOWNSAMPLING_KERNEL_05);

        value = Math.max(Math.min(value, 255), 0);

        return new GrayPixel(Math.round(value));
    }

    public static float valueMapper(GrayPixel p) {
        return p.gray();
    }

    public static GrayPixel getRandomPixel() {
        Double gray = Math.random() * 255;
        return new GrayPixel(gray.intValue());
    }

}
