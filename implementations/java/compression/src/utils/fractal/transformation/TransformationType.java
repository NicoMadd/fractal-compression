package implementations.java.compression.src.utils.fractal.transformation;

/**
 * Isometries of a square block for fractal domain–range matching.
 * <p>
 * {@code CW} / {@code CCW}: clockwise / counter-clockwise when viewing the matrix with
 * row {@code 0} at the top. {@code HZ} / {@code VC} flip columns / rows (mirror lines
 * through the block center). {@code FD} / {@code SD} are reflections in the two diagonals.
 */
public enum TransformationType {
    /** No change; pixels stay in place. */
    IDENTITY,
    /** 90° clockwise rotation; square {@code n×n}: {@code (i,j) -> (j, n-1-i)}. */
    ROTATION_CW_90,
    /** 180° rotation; {@code (i,j) -> (n-1-i, n-1-j)} (uses row count as {@code n} in code). */
    ROTATION_CW_180,
    /** 90° counter-clockwise; square {@code n×n}: {@code (i,j) -> (n-1-j, i)}. */
    ROTATION_CCW_90,
    /**
     * Left–right flip: reverses column index {@code j}, row unchanged ({@code (i,j) -> (i, n-1-j)}).
     * Mirror line is vertical through the block center.
     */
    REFLECTION_HZ,
    /**
     * Top–bottom flip: reverses row index {@code i}, column unchanged ({@code (i,j) -> (n-1-i, j)}).
     * Mirror line is horizontal through the block center.
     */
    REFLECTION_VC,
    /** Mirror in the main diagonal (top-left to bottom-right); same as transpose {@code (i,j) -> (j,i)}. */
    REFLECTION_FD,
    /** Mirror in the anti-diagonal; square {@code n×n}: {@code (i,j) -> (n-1-j, n-1-i)}. */
    REFLECTION_SD,

}
