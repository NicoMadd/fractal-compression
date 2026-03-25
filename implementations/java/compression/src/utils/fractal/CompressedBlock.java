package implementations.java.compression.src.utils.fractal;

/**
 * Best domain match for a range; {@code s} and {@code o} use RGB channel
 * ordinals.
 */
public record CompressedBlock(Block range, Block domain, float[] s, float[] o) {
}
