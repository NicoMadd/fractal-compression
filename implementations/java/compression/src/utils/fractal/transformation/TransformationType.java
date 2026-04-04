package implementations.java.compression.src.utils.fractal.transformation;

/**
 * Symmetry of the square applied to a block for fractal domain–range matching.
 * <p>
 * {@code CW} / {@code CCW}: clockwise / counter-clockwise. Reflections are
 * about axes through the block center.
 */
public enum TransformationType {
    /** No change; pixels stay in place. */
    IDENTITY,
    /** Rotate the block 90° clockwise. */
    ROTATION_CW_90,
    /** Rotate the block 180°. */
    ROTATION_CW_180,
    /** Rotate the block 90° counter-clockwise (270° clockwise). */
    ROTATION_CCW_90,
    /** Horizontal reflection over the x-axis: reverses column index {@code j} (left ↔ right). */
    REFLECTION_HZ,
    /** Vertical reflection over the y-axis: reverses row index {@code i} (top ↔ bottom). */
    REFLECTION_VC,
    /** First diagonal: mirror in the top-left–bottom-right diagonal (transpose, {@code (i,j) -> (j,i)}). */
    REFLECTION_FD,
    /** Secondary diagonal: mirror in the top-right–bottom-left diagonal ({@code (i,j) -> (n-1-j, n-1-i)}). */
    REFLECTION_SD,

}
