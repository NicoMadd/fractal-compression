package implementations.java.compression.src.utils.fractal.transformation.definitions;

import implementations.java.compression.src.utils.fractal.transformation.Transformation;
import implementations.java.compression.src.utils.fractal.transformation.TransformationType;
import implementations.java.compression.src.utils.matrix.MatrixShape;
import implementations.java.compression.src.utils.matrix.MatrixUtils;

public class RotationCW90 extends Transformation {

    public RotationCW90() {
        super(TransformationType.ROTATION_CW_90);
    }

    @Override
    public <T> void transform(T[][] src, T[][] transformed) {
        MatrixShape shape = MatrixUtils.shape(src);

        // assuming square matrix
        int dimension = shape.rows();

        for (int i = 0; i < shape.rows(); i++) {
            for (int j = 0; j < shape.cols(); j++) {

                int ti = j;
                int tj = dimension - 1 - i;

                transformed[ti][tj] = src[i][j];

            }
        }

    }

}
