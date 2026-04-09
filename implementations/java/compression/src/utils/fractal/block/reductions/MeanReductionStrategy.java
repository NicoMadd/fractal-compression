package implementations.java.compression.src.utils.fractal.block.reductions;

import implementations.java.compression.src.utils.image.pixel.GrayPixel;
import implementations.java.compression.src.utils.matrix.MatrixShape;
import implementations.java.compression.src.utils.matrix.MatrixUtils;

public class MeanReductionStrategy implements ReductionStrategy {

    @Override
    public GrayPixel[][] reduce(GrayPixel[][] pixels, int reduceTo) {

        // validate square matrix
        MatrixShape shape = MatrixUtils.shape(pixels);
        validateSquareMatrix(shape);
        validateValidReduceTo(shape, reduceTo);

        // ej nxn => mxm == reduceRatio = n/m
        int reduceRatio = shape.rows() / reduceTo;

        GrayPixel[][] reduced = new GrayPixel[reduceTo][reduceTo];

        for (int ri = 0; ri < reduceTo; ri++) {
            for (int rj = 0; rj < reduceTo; rj++) {

                int mainOffsetI = ri * reduceRatio;
                int mainOffsetJ = rj * reduceRatio;

                GrayPixel[][] innerPixels = new GrayPixel[reduceRatio][reduceRatio];
                MatrixUtils.copySquare(pixels, innerPixels, mainOffsetI, mainOffsetJ, reduceRatio);

                GrayPixel reducedPixel = reducePixel(innerPixels, reduceRatio);
                reduced[ri][rj] = reducedPixel;
            }
        }

        return reduced;

    }

    private GrayPixel reducePixel(GrayPixel[][] pixels, int reduceRatio) {

        MatrixShape shape = MatrixUtils.shape(pixels);
        float[][] values = new float[shape.rows()][shape.cols()];

        for (int i = 0; i < shape.rows(); i++) {
            for (int j = 0; j < shape.cols(); j++) {
                values[i][j] = pixels[i][j].gray();
            }
        }

        int avg = (int) MatrixUtils.dotProduct(values, MatrixUtils.avgKernel(reduceRatio));
        return new GrayPixel(avg);

    }

    private void validateValidReduceTo(MatrixShape shape, int reduceTo) {
        if (shape.rows() < reduceTo) {
            throw new IllegalArgumentException("Reduce to must be lower than the current dimension of the block.");
        }
    }

    private void validateSquareMatrix(MatrixShape shape) {
        if (shape.rows() != shape.cols()) {
            throw new IllegalArgumentException("Matrix must be square");
        }
    }
}
