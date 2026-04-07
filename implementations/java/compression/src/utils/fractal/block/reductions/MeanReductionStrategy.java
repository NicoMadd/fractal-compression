package implementations.java.compression.src.utils.fractal.block.reductions;

import implementations.java.compression.src.utils.image.pixel.GrayPixel;
import implementations.java.compression.src.utils.matrix.MatrixUtils;

public class MeanReductionStrategy implements ReductionStrategy {

    @Override
    public GrayPixel reduce(GrayPixel[][] pixels, int mainOffsetI, int mainOffsetJ, int reduceRatio) {
        float[][] innerPixels = new float[reduceRatio][reduceRatio];

        for (int ri = 0; ri < reduceRatio; ri++) {
            for (int rj = 0; rj < reduceRatio; rj++) {
                innerPixels[ri][rj] = pixels[mainOffsetI + ri][mainOffsetJ + rj].gray();
            }
        }

        int avg = (int) MatrixUtils.dotProduct(innerPixels, MatrixUtils.avgKernel(reduceRatio));

        return new GrayPixel(avg);
    }
}
