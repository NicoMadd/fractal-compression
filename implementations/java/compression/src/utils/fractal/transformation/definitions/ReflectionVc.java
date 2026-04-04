package implementations.java.compression.src.utils.fractal.transformation.definitions;

import implementations.java.compression.src.utils.fractal.transformation.Transformation;
import implementations.java.compression.src.utils.fractal.transformation.TransformationType;
import implementations.java.compression.src.utils.matrix.MatrixShape;
import implementations.java.compression.src.utils.matrix.MatrixUtils;

/**
 * Vertical reflection over the y-axis ({@code VC}): reverses the vertical
 * (row / {@code y}) coordinate, {@code i -> n-1-i}; column index {@code j} is
 * unchanged (top ↔ bottom). The mirror line is the horizontal midline of the
 * block.
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
