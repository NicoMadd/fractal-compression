package implementations.java.compression.src.utils.fractal.transformation.definitions;

import implementations.java.compression.src.utils.fractal.transformation.Transformation;
import implementations.java.compression.src.utils.fractal.transformation.TransformationType;
import implementations.java.compression.src.utils.matrix.MatrixShape;
import implementations.java.compression.src.utils.matrix.MatrixUtils;

/**
 * {@code SD}: mirror across the anti-diagonal (top-right to bottom-left). Square
 * {@code n×n}: {@code (i,j) -> (n-1-j, n-1-i)} with {@code n = shape.rows()}.
 */
public class ReflectionSd extends Transformation {

    public ReflectionSd() {
        super(TransformationType.REFLECTION_SD);
    }

    @Override
    public <T> void transform(T[][] src, T[][] transformed) {
        MatrixShape shape = MatrixUtils.shape(src);
        int n = shape.rows();

        for (int i = 0; i < shape.rows(); i++) {
            for (int j = 0; j < shape.cols(); j++) {
                int ti = n - 1 - j;
                int tj = n - 1 - i;
                transformed[ti][tj] = src[i][j];
            }
        }
    }
}
