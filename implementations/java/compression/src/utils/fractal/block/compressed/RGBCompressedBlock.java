package implementations.java.compression.src.utils.fractal.block.compressed;

import implementations.java.compression.src.utils.fractal.block.RGBBlock;

/**
 * Best domain match for a range; {@code s} and {@code o} use RGB channel
 * ordinals.
 */
public record RGBCompressedBlock(RGBBlock range, RGBBlock domain, float[] s, float[] o) {
}
