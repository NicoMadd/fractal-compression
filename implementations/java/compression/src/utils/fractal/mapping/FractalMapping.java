package implementations.java.compression.src.utils.fractal.mapping;

import implementations.java.compression.src.utils.fractal.transformation.TransformationType;

public record FractalMapping(int rangeX, int rangeY, int domainX, int domainY, float s, float o, TransformationType t) {

}
