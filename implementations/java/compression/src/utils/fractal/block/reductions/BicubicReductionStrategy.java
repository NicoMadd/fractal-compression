package implementations.java.compression.src.utils.fractal.block.reductions;

import implementations.java.compression.src.utils.image.pixel.GrayPixel;
import implementations.java.compression.src.utils.matrix.MatrixUtils;

public class BicubicReductionStrategy implements ReductionStrategy {

    private static final float[][] BICUBIC_DOWNSAMPLING_KERNEL_05 = {
            { 0.00390625f, -0.03515625f, -0.03515625f, 0.00390625f },
            { -0.03515625f, 0.31640625f, 0.31640625f, -0.03515625f },
            { -0.03515625f, 0.31640625f, 0.31640625f, -0.03515625f },
            { 0.00390625f, -0.03515625f, -0.03515625f, 0.00390625f }
    };

    @Override
    public GrayPixel reduce(GrayPixel[][] pixels, int mainOffsetI, int mainOffsetJ, int reduceRatio) {
        GrayPixel[][] neighbours = new GrayPixel[reduceRatio * 2][reduceRatio * 2];
        MatrixUtils.copySquare(pixels, neighbours, mainOffsetI - 1, mainOffsetJ - 1, reduceRatio * 2);

        float[][] grayValues = MatrixUtils.floatMap(neighbours, p -> (float) p.gray());

        float value = MatrixUtils.dotProduct(grayValues, BICUBIC_DOWNSAMPLING_KERNEL_05);

        value = Math.max(Math.min(value, 255), 0);

        return new GrayPixel(Math.round(value));
    }
}
