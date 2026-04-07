package implementations.java.compression.src.utils.fractal.block;

import implementations.java.compression.src.utils.fractal.block.reductions.ReductionStrategy;
import implementations.java.compression.src.utils.image.pixel.RGBPixel;
import implementations.java.compression.src.utils.image.pixel.utils.RGBPixelUtils;

/* 
x: is the x point of the left-top pixel of the pixels.
y: is the y point of the left-top pixel of the pixels.
*/
public class RGBBlock extends Block<RGBPixel> {

    public RGBBlock(int x, int y, RGBPixel[][] pixels) {
        super(x, y, pixels);
    }

    public float[] means() {

        int[] sums = new int[3];
        float[] means = new float[3];
        int total = 0;

        for (RGBPixel[] row : this.pixels) {
            for (RGBPixel p : row) {
                sums[0] += p.red();
                sums[1] += p.green();
                sums[2] += p.blue();
                total++;
            }
        }

        means[0] = sums[0] / total;
        means[1] = sums[1] / total;
        means[2] = sums[2] / total;

        return means;
    }

    /**
     * @param reduceTo dimension to reduce the block of pixels to
     * @return a new Block with the array reduced to a square matrix of reduceTo
     *         dim.
     */
    public RGBBlock reduce(int reduceTo, ReductionStrategy reductionStrategy) {

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
        int totalReducedPixels = (int) Math.round(Math.pow(reduceRatio, 2));

        RGBPixel[][] reducedPixels = new RGBPixel[reduceTo][reduceTo];

        for (int i = 0; i < reduceTo; i++) {
            for (int j = 0; j < reduceTo; j++) {

                int mainOffsetI = i * reduceRatio;
                int mainOffsetJ = j * reduceRatio;

                RGBPixel[] innerPixels = new RGBPixel[totalReducedPixels];
                int idx = 0;

                for (int ri = 0; ri < reduceRatio; ri++) {
                    for (int rj = 0; rj < reduceRatio; rj++) {
                        innerPixels[idx++] = this.pixels[mainOffsetI + ri][mainOffsetJ + rj];
                    }
                }

                RGBPixel reducedPixel = RGBPixelUtils.reduce(innerPixels);

                reducedPixels[i][j] = reducedPixel;

            }

        }

        RGBBlock reducedBlock = new RGBBlock(this.x(), this.y(), reducedPixels);

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
