package implementations.java.compression.src.utils.fractal.block.compressed;

import implementations.java.compression.src.utils.fractal.block.GrayBlock;
import implementations.java.compression.src.utils.fractal.transformation.TransformationType;

/**
 * Represents the best matching domain block and associated transformation
 * parameters (s, o)
 * for a given range block in fractal image compression.
 * <p>
 * <b>Fields:</b>
 * <ul>
 * <li>range - The target range block to be approximated.</li>
 * <li>domain - The domain block that best matches the range after
 * transformation.</li>
 * <li>s - The contrast scaling factor (applied to the domain block).</li>
 * <li>o - The offset/brightness adjustment (applied after scaling).</li>
 * </ul>
 * Both {@code s} and {@code o} are parameters of the affine transformation
 * applied to the pixel values.
 */
public record GrayCompressedBlock(GrayBlock range, GrayBlock domain, float s, float o, TransformationType t) {
}
