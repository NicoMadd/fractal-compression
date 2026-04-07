package implementations.java.compression.src.utils.fractal.block.reductions;

import implementations.java.compression.src.utils.image.pixel.GrayPixel;
import implementations.java.compression.src.utils.image.pixel.utils.GrayPixelUtils;

public class BicubicReductionStrategy implements ReductionStrategy {

    @Override
    public GrayPixel reduce(GrayPixel[][] pixels, int mainOffsetI, int mainOffsetJ, int reduceRatio) {
        return GrayPixelUtils.bicubicReduction(pixels, mainOffsetI, mainOffsetJ, reduceRatio);
    }
}
