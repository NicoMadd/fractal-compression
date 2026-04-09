package implementations.java.compression.src.utils.fractal.block.reductions;

import implementations.java.compression.src.utils.image.pixel.GrayPixel;

public interface ReductionStrategy {

    public GrayPixel[][] reduce(GrayPixel[][] pixels, int reduceTo);
}
