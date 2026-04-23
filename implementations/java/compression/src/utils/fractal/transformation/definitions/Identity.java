package implementations.java.compression.src.utils.fractal.transformation.definitions;

import implementations.java.compression.src.utils.fractal.transformation.Transformation;
import implementations.java.compression.src.utils.fractal.transformation.TransformationType;
import implementations.java.compression.src.utils.matrix.MatrixUtils;

/** {@code IDENTITY}: copies {@code src} into {@code transformed}. */
public class Identity extends Transformation {

    public Identity() {
        super(TransformationType.IDENTITY);
    }

    @Override
    public <T> void transform(T[][] src, T[][] transformed) {
        MatrixUtils.copy(src, transformed);
    }

}
