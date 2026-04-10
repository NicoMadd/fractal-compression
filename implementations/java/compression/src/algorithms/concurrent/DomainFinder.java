package implementations.java.compression.src.algorithms.concurrent;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import implementations.java.compression.src.utils.fractal.block.GrayBlock;
import implementations.java.compression.src.utils.fractal.block.compressed.GrayCompressedBlock;
import implementations.java.compression.src.utils.fractal.mapping.FractalMapping;
import implementations.java.compression.src.utils.fractal.reducedpair.GrayReducedPair;
import implementations.java.compression.src.utils.fractal.transformation.Transformation;
import implementations.java.compression.src.utils.fractal.transformation.TransformationFactory;
import implementations.java.compression.src.utils.fractal.transformation.TransformationType;
import implementations.java.compression.src.utils.image.pixel.GrayPixel;
import implementations.java.compression.src.utils.matrix.MatrixUtils;

public class DomainFinder implements Callable<FractalMapping> {

    public static List<Transformation> ALLOWED_TRANSFORMATIONS = Arrays.stream(TransformationType.values())
            .map(TransformationFactory::of)
            .toList();

    private GrayBlock range;
    private GrayReducedPair[][] reducedPairs;
    private float meanR;
    private int rangeSize;
    private AtomicInteger counter;
    private int totalRanges;
    private int step;

    public DomainFinder(GrayReducedPair[][] reducedPairs, GrayBlock range, AtomicInteger counter, int totalRanges) {
        this.reducedPairs = reducedPairs;
        this.range = range;
        this.meanR = range.mean();
        this.rangeSize = MatrixUtils.shape(range.pixels()).rows();
        this.counter = counter;
        this.totalRanges = totalRanges;
        this.step = Math.max(1, totalRanges / 25);

    }

    @Override
    public FractalMapping call() throws Exception {

        GrayCompressedBlock bestDomain = findBestDomainMatch(range, reducedPairs);
        FractalMapping fm = new FractalMapping(range.x(), range.y(), bestDomain.domain().x(),
                bestDomain.domain().y(), bestDomain.s(), bestDomain.o(), bestDomain.t());

        int done = counter.incrementAndGet();

        if (done == 1 || done == totalRanges || done % step == 0) {
            System.out.println("Compress: matched range blocks " + done + "/" + totalRanges);
        }

        return fm;
    }

    /**
     * Calculate S is the summation of the differences between each Di minus the
     * meansD times
     * the difference between Ri minuts the meansR.
     * 
     * sums of [ (Di - meansD) * (Ri - meansR) ]
     * 
     * @param rangePixels n pixels unordered composing the range block
     * @param rdPixels    n pixels unordered composing the reduced domain block
     * @param meansD
     * @param meansR
     * @return s value calculated for these blocks
     * 
     */
    private float calculateS(GrayPixel[][] rangePixels, GrayPixel[][] rdPixels, float meanD) {
        // calculate s
        // numerator: summation of the diff between each
        // Di minus the avgD times
        // the diff between Ri minus the avgR.

        float numerator = 0;
        float denominator = 0;

        for (int i = 0; i < rangeSize; i++) {
            for (int j = 0; j < rangeSize; j++) {
                int domainColorValue = rdPixels[i][j].gray();
                int rangeColorValue = rangePixels[i][j].gray();

                float domainDiff = domainColorValue - meanD;
                float rangeDiff = rangeColorValue - this.meanR;

                numerator += domainDiff * rangeDiff;
                denominator += Math.pow(domainDiff, 2);
            }
        }

        float s = 0;

        if (denominator != 0) {
            s = numerator / denominator;
        }

        return s;
    }

    private float calculateO(float meanD, float s) {
        return this.meanR - s * meanD;
    }

    private GrayCompressedBlock findBestDomainMatch(GrayBlock range,
            GrayReducedPair[][] reducedDomainsPairs) {

        this.meanR = range.mean();

        GrayCompressedBlock bestCompression = null;
        float bestError = Float.MAX_VALUE;

        for (GrayReducedPair[] row : reducedDomainsPairs) {
            for (GrayReducedPair rdp : row) {
                GrayBlock rd = rdp.reduced();
                float meanD = rd.mean();

                GrayPixel[][] rangePixels = range.pixels();
                GrayPixel[][] rdPixels = rd.pixels();

                for (Transformation t : ALLOWED_TRANSFORMATIONS) {

                    float blockError = 0;

                    GrayPixel[][] transformedRD = new GrayPixel[rangeSize][rangeSize];
                    t.transform(rdPixels, transformedRD);

                    float s = calculateS(rangePixels, transformedRD, meanD);

                    // calculate o
                    float o = calculateO(meanD, s);

                    // Calculate color error
                    float colorError = 0;

                    for (int i = 0; i < rangeSize; i++) {
                        for (int j = 0; j < rangeSize; j++) {
                            float approxRange = s * transformedRD[i][j].gray() + o;

                            float rangeDiff = rangePixels[i][j].gray() - approxRange;

                            colorError += Math.pow(rangeDiff, 2);
                        }
                    }

                    // sum color error to block error
                    blockError += colorError;

                    // decide wether to keep this block or discard it.
                    // if block error is less than the actual best, then keep it.
                    if (blockError < bestError) {
                        bestCompression = new GrayCompressedBlock(range, rdp.domain(), s, o, t.type());
                        bestError = blockError;
                    }
                }
            }
        }

        return bestCompression;

    }

}
