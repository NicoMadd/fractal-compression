package implementations.java.compression.src.utils.fractal.transformation;

/**
 * Maps each cell of {@code src} to exactly one cell of {@code transformed} (a permutation
 * of indices for the transforms in this package). {@code src[i][j]} is row {@code i},
 * column {@code j}. Callers allocate {@code transformed} with the same shape as {@code src}
 * for rotations and reflections used in compression.
 */
public abstract class Transformation {

    protected TransformationType type;

    public Transformation(TransformationType type) {
        this.type = type;
    }

    public TransformationType type() {
        return this.type;
    }

    /**
     * Writes {@code transformed[r][c]} from {@code src} according to this transform.
     *
     * @param src         source block; first index row, second column
     * @param transformed destination block, same dimensions as {@code src} for codec usage
     */
    public abstract <T> void transform(T[][] src, T[][] transformed);

}
