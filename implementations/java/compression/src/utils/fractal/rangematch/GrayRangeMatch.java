package implementations.java.compression.src.utils.fractal.rangematch;

import implementations.java.compression.src.utils.fractal.block.GrayBlock;
import implementations.java.compression.src.utils.fractal.block.compressed.GrayCompressedBlock;

/**
 * A range block paired with its best domain match
 * ({@link GrayCompressedBlock}).
 */
public record GrayRangeMatch(GrayBlock range, GrayCompressedBlock compressed) {
}
