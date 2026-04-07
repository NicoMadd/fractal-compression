package implementations.java.compression.src.utils.fractal.block;

import implementations.java.compression.src.utils.fractal.block.reductions.ReductionStrategy;
import implementations.java.compression.src.utils.image.pixel.GrayPixel;

/* 
x: is the x point of the left-top pixel of the pixels.
y: is the y point of the left-top pixel of the pixels.
*/
public class GrayBlock extends Block<GrayPixel> {

    public GrayBlock(int x, int y, GrayPixel[][] pixels) {
        super(x, y, pixels);
    }

    public float mean() {

        int sum = 0;
        int total = 0;

        for (GrayPixel[] row : pixels) {
            for (GrayPixel p : row) {
                sum += p.gray();
                total++;
            }
        }

        return sum / total;

    }

    /**
     * @param reduceTo dimension to reduce the block of pixels to
     * @return a new Block with the array reduced to a square matrix of reduceTo
     *         dim.
     */
    public GrayBlock reduce(int reduceTo, ReductionStrategy reductionStrategy) {

        int blockDim = this.pixels.length;

        if (reduceTo > blockDim) {
            throw new IllegalArgumentException("reduceTo cannot be bigger than the actual block dimension.");
        }

        if ((blockDim / reduceTo) % 2 != 0) {
            throw new IllegalArgumentException("reduceTo ratio should be an even number");
        }

        // calculo para saber el tamaño de bloque reducido => 8x8 a 4x4 => ratio de 2 =>
        // bloques de 2x2
        int reduceRatio = blockDim / reduceTo;

        GrayPixel[][] reducedPixels = new GrayPixel[reduceTo][reduceTo];

        for (int i = 0; i < reduceTo; i++) {
            for (int j = 0; j < reduceTo; j++) {

                int mainOffsetI = i * reduceRatio;
                int mainOffsetJ = j * reduceRatio;

                GrayPixel reducedPixel = reductionStrategy.reduce(this.pixels, mainOffsetI,
                        mainOffsetJ, reduceRatio);

                reducedPixels[i][j] = reducedPixel;

            }

        }

        GrayBlock reducedBlock = new GrayBlock(this.x(), this.y(), reducedPixels);

        return reducedBlock;
    }

    public String toString() {
        int rows = this.pixels().length;

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < this.pixels()[i].length; j++) {
                sb.append(this.pixels()[i][j]);
                sb.append(' ');
            }
            sb.append('\n');
        }

        return sb.toString();

    }

}
