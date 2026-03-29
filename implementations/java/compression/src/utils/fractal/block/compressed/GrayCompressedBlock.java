package implementations.java.compression.src.utils.fractal.block.compressed;

import implementations.java.compression.src.utils.fractal.block.GrayBlock;

/**
 * Best domain match for a range; {@code s} and {@code o} use RGB channel
 * ordinals.
 */
public record GrayCompressedBlock(GrayBlock range, GrayBlock domain, float s, float o) {
}
