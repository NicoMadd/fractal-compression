package implementations.java.compression.src.algorithms;

import java.util.ArrayList;
import java.util.List;

import implementations.java.compression.src.utils.fractal.block.GrayBlock;
import implementations.java.compression.src.utils.fractal.block.compressed.GrayCompressedBlock;
import implementations.java.compression.src.utils.fractal.mapping.FractalMapping;
import implementations.java.compression.src.utils.fractal.reducedpair.GrayReducedPair;
import implementations.java.compression.src.utils.image.ImageMetadata;
import implementations.java.compression.src.utils.image.pixel.GrayPixel;
import implementations.java.compression.src.utils.matrix.MatrixUtils;

public class GrayBlockCompression {

    private GrayPixel[][] imagePixels;
    private int imageWidth;
    private int imageHeight;

    private GrayBlock[][] rangeBlocks;
    private GrayBlock[][] domainBlocks;

    private float meanR;

    // FIXME HARD VALUES
    // RANGE BLOCK DIMENSION
    public static int RBD = 4;
    // DOMAIN BLOCK DIMENSION
    public static int DBD = 8;

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
    private float calculateS(List<GrayPixel> rangePixels, List<GrayPixel> rdPixels, float meanD) {
        // calculate s
        // numerator: summation of the diff between each
        // Di minus the avgD times
        // the diff between Ri minus the avgR.

        float numerator = 0;
        float denominator = 0;

        int totalPixels = rangePixels.size();

        for (int t = 0; t < totalPixels; t++) {
            int domainColorValue = rdPixels.get(t).gray();
            int rangeColorValue = rangePixels.get(t).gray();

            float domainDiff = domainColorValue - meanD;
            float rangeDiff = rangeColorValue - this.meanR;

            numerator += domainDiff * rangeDiff;
            denominator += Math.pow(domainDiff, 2);
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

                List<GrayPixel> rangePixels = MatrixUtils.flatMap(range.pixels());
                List<GrayPixel> rdPixels = MatrixUtils.flatMap(rd.pixels());

                int totalPixels = rangePixels.size();

                // composed of all 3 channels
                float blockError = 0;

                float s = calculateS(rangePixels, rdPixels, meanD);

                // calculate o
                float o = calculateO(meanD, s);

                // Calculate color error
                float colorError = 0;

                for (int t = 0; t < totalPixels; t++) {
                    float approxRange = s * rdPixels.get(t).gray() + o;

                    float rangeDiff = rangePixels.get(t).gray() - approxRange;

                    colorError += Math.pow(rangeDiff, 2);
                }

                // sum color error to block error
                blockError += colorError;

                // decide wether to keep this block or discard it.
                // if block error is less than the actual best, then keep it.
                if (blockError < bestError) {
                    bestCompression = new GrayCompressedBlock(range, rdp.domain(), s, o);
                    bestError = blockError;
                }
            }
        }

        return bestCompression;

    }

    private GrayBlock[][] buildRangeBlocks(int rangeBlocksNumber) {
        GrayBlock[][] blocks = new GrayBlock[rangeBlocksNumber][rangeBlocksNumber];

        for (int i = 0; i < rangeBlocksNumber; i++) {
            for (int j = 0; j < rangeBlocksNumber; j++) {
                GrayPixel[][] blockPixels = new GrayPixel[RBD][RBD];

                for (int k = 0; k < RBD; k++) {
                    for (int l = 0; l < RBD; l++) {
                        blockPixels[k][l] = this.imagePixels[RBD * i + k][RBD * j + l];
                    }
                }

                blocks[i][j] = new GrayBlock(RBD * i, RBD * j, blockPixels);
            }
        }

        return blocks;
    }

    private GrayBlock[][] buildDomainBlocks(int domainBlockNumber) {
        GrayBlock[][] blocks = new GrayBlock[domainBlockNumber][domainBlockNumber];

        for (int i = 0; i < domainBlockNumber; i++) {
            for (int j = 0; j < domainBlockNumber; j++) {
                GrayPixel[][] blockPixels = new GrayPixel[DBD][DBD];

                for (int k = 0; k < DBD; k++) {
                    for (int l = 0; l < DBD; l++) {
                        blockPixels[k][l] = this.imagePixels[DBD * i + k][DBD * j + l];
                    }
                }

                blocks[i][j] = new GrayBlock(DBD * i, DBD * j, blockPixels);
            }
        }

        return blocks;
    }

    private GrayReducedPair[][] buildReducedDomainPairs(int domainBlockNumber) {
        GrayReducedPair[][] reducedDomainBlocksPair = new GrayReducedPair[domainBlockNumber][domainBlockNumber];

        for (int i = 0; i < domainBlockNumber; i++) {
            for (int j = 0; j < domainBlockNumber; j++) {
                GrayBlock reducedBlock = domainBlocks[i][j].reduce(RBD);
                reducedDomainBlocksPair[i][j] = new GrayReducedPair(domainBlocks[i][j], reducedBlock);
            }
        }

        return reducedDomainBlocksPair;
    }

    private List<FractalMapping> buildFractalMappings(GrayReducedPair[][] reducedDomainBlocksPair) {
        List<FractalMapping> mappings = new ArrayList<>();

        for (GrayBlock[] rangesRow : rangeBlocks) {
            for (GrayBlock range : rangesRow) {
                GrayCompressedBlock bestDomain = findBestDomainMatch(range, reducedDomainBlocksPair);
                FractalMapping fm = new FractalMapping(range.x(), range.y(), bestDomain.domain().x(),
                        bestDomain.domain().y(), bestDomain.s(), bestDomain.o());
                mappings.add(fm);
            }
        }

        return mappings;
    }

    public List<FractalMapping> compress(ImageMetadata<GrayPixel> metadata) {

        this.imagePixels = metadata.getPixels();
        this.imageWidth = metadata.getWidth();
        this.imageHeight = metadata.getHeight();

        int rangeBlocksNumber = this.imageWidth / RBD; // 256 / 4 = 64
        int domainBlockNumber = this.imageHeight / DBD; // 256 / (4 * 2) = 32

        this.rangeBlocks = buildRangeBlocks(rangeBlocksNumber);
        this.domainBlocks = buildDomainBlocks(domainBlockNumber);

        GrayReducedPair[][] reducedDomainBlocksPair = buildReducedDomainPairs(domainBlockNumber);

        System.out.println("Compression Finished!");
        return buildFractalMappings(reducedDomainBlocksPair);
    }

}
