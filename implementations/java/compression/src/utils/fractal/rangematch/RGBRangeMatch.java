package implementations.java.compression.src.utils.fractal.rangematch;

import implementations.java.compression.src.utils.fractal.block.RGBBlock;
import implementations.java.compression.src.utils.fractal.block.compressed.RGBCompressedBlock;

/**
 * A range block paired with its best domain match ({@link RGBCompressedBlock}).
 */
public record RGBRangeMatch(RGBBlock range, RGBCompressedBlock compressed) {
}
