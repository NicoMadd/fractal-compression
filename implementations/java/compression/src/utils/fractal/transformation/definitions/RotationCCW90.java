package implementations.java.compression.src.utils.fractal.transformation.definitions;

import implementations.java.compression.src.utils.fractal.transformation.Transformation;
import implementations.java.compression.src.utils.fractal.transformation.TransformationType;
import implementations.java.compression.src.utils.matrix.MatrixShape;
import implementations.java.compression.src.utils.matrix.MatrixUtils;

public class RotationCCW90 extends Transformation {

    public RotationCCW90() {
        super(TransformationType.ROTATION_CCW_90);
    }

    @Override
    public <T> void transform(T[][] src, T[][] transformed) {
        MatrixShape shape = MatrixUtils.shape(src);

        // assuming square matrix
        int dimension = shape.rows();

        for (int i = 0; i < shape.rows(); i++) {
            for (int j = 0; j < shape.cols(); j++) {

                int ti = dimension - 1 - j;
                int tj = i;

                transformed[ti][tj] = src[i][j];

            }
        }

    }

}
