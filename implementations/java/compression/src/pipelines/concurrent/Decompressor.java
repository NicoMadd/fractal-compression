package implementations.java.compression.src.pipelines.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import implementations.java.compression.src.utils.fractal.block.GrayBlock;
import implementations.java.compression.src.utils.fractal.block.reductions.ReductionStrategy;
import implementations.java.compression.src.utils.fractal.mapping.FractalMapping;
import implementations.java.compression.src.utils.fractal.transformation.Transformation;
import implementations.java.compression.src.utils.image.pixel.GrayPixel;
import implementations.java.compression.src.utils.matrix.MatrixUtils;

public class Decompressor implements Callable<Void> {

    private FractalMapping fm;
    private int domainSize;
    private int rangeSize;
    private ReductionStrategy rs;
    private GrayPixel[][] image;
    private GrayPixel[][] next;
    private CountDownLatch latch;

    public Decompressor(FractalMapping fm,
            int domainSize,
            int rangeSize,
            ReductionStrategy rs,
            GrayPixel[][] image,
            GrayPixel[][] next,
            CountDownLatch latch) {
        this.fm = fm;
        this.domainSize = domainSize;
        this.rangeSize = rangeSize;
        this.rs = rs;
        this.image = image;
        this.next = next;
        this.latch = latch;
    }

    @Override
    public Void call() throws Exception {
        try {

            int xDomain = fm.domainX();
            int yDomain = fm.domainY();

            GrayPixel[][] domainPixels = new GrayPixel[this.domainSize][this.domainSize];

            MatrixUtils.copySquare(this.image, domainPixels, xDomain, yDomain, this.domainSize);

            GrayBlock domainBlock = new GrayBlock(xDomain, yDomain, domainPixels);

            GrayBlock reducedBlock = domainBlock.reduce(this.rangeSize, rs);

            GrayPixel[][] newPixels = new GrayPixel[this.rangeSize][this.rangeSize];

            GrayPixel[][] transformedPixels = new GrayPixel[this.rangeSize][this.rangeSize];

            Transformation t = fm.transformation();

            t.transform(reducedBlock.pixels(), transformedPixels);

            for (int i = 0; i < this.rangeSize; i++) {
                for (int j = 0; j < this.rangeSize; j++) {

                    GrayPixel p = transformedPixels[i][j];

                    // gray has the same value for all colors
                    // here we take red to simplify it, for now.
                    float gray = p.gray();

                    // for gray its just one pixel for s and o.
                    float s = fm.s();
                    float o = fm.o();

                    // new generated value with s/o
                    int newColorValue = Math.max(0, Math.min(255, (int) (s * gray + o)));

                    GrayPixel newPixel = new GrayPixel(newColorValue);

                    newPixels[i][j] = newPixel;
                }
            }

            // use range in rm to write the new pixels in next pixels

            int xRangeOffset = fm.rangeX();
            int yRangeOffset = fm.rangeY();

            for (int i = 0; i < this.rangeSize; i++) {
                for (int j = 0; j < this.rangeSize; j++) {
                    this.next[xRangeOffset + i][yRangeOffset + j] = newPixels[i][j];
                }
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            this.latch.countDown();
        }

        return null;
    }

}
