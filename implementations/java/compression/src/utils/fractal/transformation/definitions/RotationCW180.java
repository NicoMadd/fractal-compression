package implementations.java.compression.src.utils.fractal.transformation.definitions;

import implementations.java.compression.src.utils.fractal.transformation.Transformation;
import implementations.java.compression.src.utils.fractal.transformation.TransformationType;
import implementations.java.compression.src.utils.matrix.MatrixShape;
import implementations.java.compression.src.utils.matrix.MatrixUtils;

/**
 * 180° rotation. With {@code n = shape.rows()}: {@code (i,j) -> (n-1-i, n-1-j)}; square
 * blocks use the same {@code n} for rows and columns.
 */
public class RotationCW180 extends Transformation {

    public RotationCW180() {
        super(TransformationType.ROTATION_CW_180);
    }

    @Override
    public <T> void transform(T[][] src, T[][] transformed) {
        MatrixShape shape = MatrixUtils.shape(src);
        int dimension = shape.rows();

        for (int i = 0; i < shape.rows(); i++) {
            for (int j = 0; j < shape.cols(); j++) {

                int ti = dimension - 1 - i;
                int tj = dimension - 1 - j;

                transformed[ti][tj] = src[i][j];

            }
        }

    }

}
