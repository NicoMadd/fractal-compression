package implementations.java.compression.src.utils.fractal.block.reductions;

import implementations.java.compression.src.utils.image.pixel.GrayPixel;
import implementations.java.compression.src.utils.matrix.MatrixShape;
import implementations.java.compression.src.utils.matrix.MatrixUtils;

public class BicubicReductionStrategy implements ReductionStrategy {

    private static final float[][] BICUBIC_DOWNSAMPLING_KERNEL_05 = {
            { 0.00390625f, -0.03515625f, -0.03515625f, 0.00390625f },
            { -0.03515625f, 0.31640625f, 0.31640625f, -0.03515625f },
            { -0.03515625f, 0.31640625f, 0.31640625f, -0.03515625f },
            { 0.00390625f, -0.03515625f, -0.03515625f, 0.00390625f }
    };

    @Override
    public GrayPixel[][] reduce(GrayPixel[][] pixels, int reduceTo) {
        // validate square matrix
        MatrixShape shape = MatrixUtils.shape(pixels);
        validateSquareMatrix(shape);
        validateValidReduceTo(shape, reduceTo);

        int reduceRatio = shape.rows() / reduceTo;

        if (reduceRatio % 2 != 0) {
            throw new IllegalArgumentException("Reduce ratio should be an even number");
        }

        GrayPixel[][] reduced = new GrayPixel[reduceTo][reduceTo];

        System.out.println(shape);

        for (int ri = 0; ri < reduceTo; ri++) {
            for (int rj = 0; rj < reduceTo; rj++) {

                int mainOffsetI = ri * reduceRatio;
                int mainOffsetJ = rj * reduceRatio;

                GrayPixel[][] innerPixels = new GrayPixel[4][4];
                MatrixUtils.copySquare(pixels, innerPixels, mainOffsetI - 1, mainOffsetJ - 1, 4);

                GrayPixel reducedPixel = reducePixel(innerPixels);
                reduced[ri][rj] = reducedPixel;
            }
        }

        return reduced;

    }

    private void validateSquareMatrix(MatrixShape shape) {
        if (shape.rows() != shape.cols()) {
            throw new IllegalArgumentException("Matrix must be square");
        }
    }

    private void validateValidReduceTo(MatrixShape shape, int reduceTo) {
        if (shape.rows() < reduceTo) {
            throw new IllegalArgumentException("Reduce to must be lower than the current dimension of the block.");
        }
    }

    private GrayPixel reducePixel(GrayPixel[][] pixels) {
        float[][] grayValues = MatrixUtils.floatMap(pixels, p -> (float) p.gray());

        System.out.println(MatrixUtils.shape(pixels));

        float value = MatrixUtils.dotProduct(grayValues, BICUBIC_DOWNSAMPLING_KERNEL_05);

        value = Math.max(Math.min(value, 255), 0);

        return new GrayPixel(Math.round(value));
    }
}
