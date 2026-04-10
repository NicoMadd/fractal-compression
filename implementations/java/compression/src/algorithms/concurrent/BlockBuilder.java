package implementations.java.compression.src.algorithms.concurrent;

import java.util.concurrent.Callable;

import implementations.java.compression.src.utils.fractal.block.GrayBlock;
import implementations.java.compression.src.utils.image.pixel.GrayPixel;

public class BlockBuilder implements Callable<GrayBlock> {

    private GrayPixel[][] pixels;

    private int x;
    private int y;
    private int dimension;

    public BlockBuilder(GrayPixel[][] pixels, int x, int y, int dimension) {
        this.pixels = pixels;
        this.x = x;
        this.y = y;
        this.dimension = dimension;

    }

    @Override
    public GrayBlock call() throws Exception {
        GrayPixel[][] blockPixels = new GrayPixel[dimension][dimension];

        for (int k = 0; k < dimension; k++) {
            for (int l = 0; l < dimension; l++) {
                blockPixels[k][l] = this.pixels[x + k][y + l];
            }
        }

        return new GrayBlock(x, y, blockPixels);
    }

}
