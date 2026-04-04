package implementations.java.compression.src.utils.fractal.transformation.definitions;

import implementations.java.compression.src.utils.fractal.transformation.Transformation;
import implementations.java.compression.src.utils.fractal.transformation.TransformationType;
import implementations.java.compression.src.utils.matrix.MatrixShape;
import implementations.java.compression.src.utils.matrix.MatrixUtils;

/**
 * First-diagonal reflection ({@code FD}): mirror across the primary diagonal
 * (top-left to bottom-right). Equivalent to matrix transpose: {@code (i,j) -> (j,i)}.
 */
public class ReflectionFd extends Transformation {

    public ReflectionFd() {
        super(TransformationType.REFLECTION_FD);
    }

    @Override
    public <T> void transform(T[][] src, T[][] transformed) {
        MatrixShape shape = MatrixUtils.shape(src);

        for (int i = 0; i < shape.rows(); i++) {
            for (int j = 0; j < shape.cols(); j++) {
                int ti = j;
                int tj = i;
                transformed[ti][tj] = src[i][j];
            }
        }
    }
}
