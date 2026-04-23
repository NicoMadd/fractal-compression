package implementations.java.compression.src.utils.fractal.transformation.definitions;

import implementations.java.compression.src.utils.fractal.transformation.Transformation;
import implementations.java.compression.src.utils.fractal.transformation.TransformationType;
import implementations.java.compression.src.utils.matrix.MatrixShape;
import implementations.java.compression.src.utils.matrix.MatrixUtils;

/**
 * {@code VC}: top–bottom flip, {@code (i,j) -> (n-1-i, j)} with {@code n = shape.rows()}
 * (square blocks). Mirror line is horizontal through the block center.
 */
public class ReflectionVc extends Transformation {

    public ReflectionVc() {
        super(TransformationType.REFLECTION_VC);
    }

    @Override
    public <T> void transform(T[][] src, T[][] transformed) {
        MatrixShape shape = MatrixUtils.shape(src);
        int n = shape.rows();

        for (int i = 0; i < shape.rows(); i++) {
            for (int j = 0; j < shape.cols(); j++) {
                int ti = n - 1 - i;
                int tj = j;
                transformed[ti][tj] = src[i][j];
            }
        }
    }
}
