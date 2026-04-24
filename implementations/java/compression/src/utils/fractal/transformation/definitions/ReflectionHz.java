package implementations.java.compression.src.utils.fractal.transformation.definitions;

import implementations.java.compression.src.utils.fractal.transformation.Transformation;
import implementations.java.compression.src.utils.fractal.transformation.TransformationType;
import implementations.java.compression.src.utils.matrix.MatrixShape;
import implementations.java.compression.src.utils.matrix.MatrixUtils;

/**
 * {@code HZ}: left–right flip, {@code (i,j) -> (i, n-1-j)} with {@code n = shape.rows()}
 * (square blocks in the codec, so {@code n} matches the column count). Mirror line is
 * vertical through the block center.
 */
public class ReflectionHz extends Transformation {

    public ReflectionHz() {
        super(TransformationType.REFLECTION_HZ);
    }

    @Override
    public <T> void transform(T[][] src, T[][] transformed) {
        MatrixShape shape = MatrixUtils.shape(src);
        int n = shape.rows();

        for (int i = 0; i < shape.rows(); i++) {
            for (int j = 0; j < shape.cols(); j++) {
                int ti = i;
                int tj = n - 1 - j;
                transformed[ti][tj] = src[i][j];
            }
        }
    }
}
