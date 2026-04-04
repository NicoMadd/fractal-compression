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
    /** Reflect across the vertical midline (left–right flip). */
    REFLECTION_HZ,
    /** Reflect across the horizontal midline (top–bottom flip). */
    REFLECTION_VC,
    /** Reflect across the main diagonal (top-left to bottom-right). */
    REFLECTION_FD,
    /** Reflect across the anti-diagonal (top-right to bottom-left). */
    REFLECTION_SD,

}
