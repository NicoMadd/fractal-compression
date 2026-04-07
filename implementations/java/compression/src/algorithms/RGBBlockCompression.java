package implementations.java.compression.src.algorithms;

import java.util.ArrayList;
import java.util.List;

import implementations.java.compression.src.utils.fractal.block.RGBBlock;
import implementations.java.compression.src.utils.fractal.block.compressed.RGBCompressedBlock;
import implementations.java.compression.src.utils.fractal.block.reductions.ReductionStrategy;
import implementations.java.compression.src.utils.fractal.rangematch.RGBRangeMatch;
import implementations.java.compression.src.utils.fractal.reducedpair.RGBReducedPair;
import implementations.java.compression.src.utils.image.ImageMetadata;
import implementations.java.compression.src.utils.image.pixel.RGB;
import implementations.java.compression.src.utils.image.pixel.RGBPixel;
import implementations.java.compression.src.utils.matrix.MatrixUtils;

public class RGBBlockCompression {

    private RGBPixel[][] imagePixels;
    private int imageWidth;
    private int imageHeight;

    private RGBBlock[][] rangeBlocks;
    private RGBBlock[][] domainBlocks;

    private float[] meansR;

    // FIXME HARD VALUES
    // RANGE BLOCK DIMENSION
    public static int RBD = 4;
    // DOMAIN BLOCK DIMENSION
    public static int DBD = 8;

    private ReductionStrategy reductionStrategy;

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
    private float calculateS(List<RGBPixel> rangePixels, List<RGBPixel> rdPixels, float[] meansD, RGB c) {
        // calculate s
        // numerator: summation of the diff between each
        // Di minus the avgD times
        // the diff between Ri minus the avgR.

        float numerator = 0;
        float denominator = 0;

        int totalPixels = rangePixels.size();

        for (int t = 0; t < totalPixels; t++) {
            int domainColorValue = rdPixels.get(t).color(c);
            int rangeColorValue = rangePixels.get(t).color(c);

            float domainDiff = domainColorValue - meansD[c.ordinal()];
            float rangeDiff = rangeColorValue - this.meansR[c.ordinal()];

            numerator += domainDiff * rangeDiff;
            denominator += Math.pow(domainDiff, 2);
        }

        float s = 0;

        if (denominator != 0) {
            s = numerator / denominator;
        }

        return s;
    }

    private float calculateO(float[] meansD, RGB c, float s) {
        return this.meansR[c.ordinal()] - s * meansD[c.ordinal()];
    }

    private RGBCompressedBlock findBestDomainMatch(RGBBlock range, RGBReducedPair[][] reducedDomainsPairs) {

        this.meansR = range.means();

        RGBCompressedBlock bestCompression = null;
        float bestError = Float.MAX_VALUE;

        for (RGBReducedPair[] row : reducedDomainsPairs) {
            for (RGBReducedPair rdp : row) {
                RGBBlock rd = rdp.reduced();
                float[] meansD = rd.means();

                List<RGBPixel> rangePixels = MatrixUtils.flatMap(range.pixels());
                List<RGBPixel> rdPixels = MatrixUtils.flatMap(rd.pixels());

                int totalPixels = rangePixels.size();

                // composed of all 3 channels
                float blockError = 0;

                float[] sArr = new float[3];
                float[] oArr = new float[3];

                // for each colour 0 red 1 green 2 blue
                for (RGB c : RGB.values()) {

                    float s = calculateS(rangePixels, rdPixels, meansD, c);

                    // calculate o
                    float o = calculateO(meansD, c, s);

                    // Calculate color error
                    float colorError = 0;

                    for (int t = 0; t < totalPixels; t++) {
                        float approxRange = s * rdPixels.get(t).color(c) + o;

                        float rangeDiff = rangePixels.get(t).color(c) - approxRange;

                        colorError += Math.pow(rangeDiff, 2);
                    }

                    // sum color error to block error
                    blockError += colorError;

                    // save s and o on the arrays
                    sArr[c.ordinal()] = s;
                    oArr[c.ordinal()] = o;
                }

                // decide wether to keep this block or discard it.
                // if block error is less than the actual best, then keep it.
                if (blockError < bestError) {
                    bestCompression = new RGBCompressedBlock(range, rdp.domain(), sArr, oArr);
                    bestError = blockError;
                }
            }
        }

        return bestCompression;

    }

    public List<RGBRangeMatch> compress(ImageMetadata<RGBPixel> metadata) {

        this.imagePixels = metadata.getPixels();
        this.imageWidth = metadata.getWidth();
        this.imageHeight = metadata.getHeight();

        int rangeBlocksNumber = this.imageWidth / RBD; // 256 / 4 = 64
        int domainBlockNumber = this.imageHeight / DBD; // 256 / (4 * 2) = 32

        // create 4x4 blocks - range blocks - BxB blocks
        this.rangeBlocks = new RGBBlock[rangeBlocksNumber][rangeBlocksNumber];

        for (int i = 0; i < rangeBlocksNumber; i++) {
            for (int j = 0; j < rangeBlocksNumber; j++) {
                RGBPixel[][] blockPixels = new RGBPixel[RBD][RBD];

                for (int k = 0; k < RBD; k++) {
                    for (int l = 0; l < RBD; l++) {
                        blockPixels[k][l] = this.imagePixels[RBD * i + k][RBD * j + l];
                    }
                }

                RGBBlock block = new RGBBlock(4 * i, 4 * j, blockPixels);
                rangeBlocks[i][j] = block;
            }
        }

        // create 8x8 blocks - domain blocks - 2Bx2B - DxD
        this.domainBlocks = new RGBBlock[domainBlockNumber][domainBlockNumber];

        for (int i = 0; i < domainBlockNumber; i++) {
            for (int j = 0; j < domainBlockNumber; j++) {
                RGBPixel[][] blockPixels = new RGBPixel[DBD][DBD];

                for (int k = 0; k < DBD; k++) {
                    for (int l = 0; l < DBD; l++) {

                        blockPixels[k][l] = this.imagePixels[DBD * i + k][DBD * j + l];
                    }
                }

                RGBBlock block = new RGBBlock(DBD * i, DBD * j, blockPixels);
                domainBlocks[i][j] = block;
            }
        }

        // For each domain find the reduced domain block.
        RGBReducedPair[][] reducedDomainBlocksPair = new RGBReducedPair[domainBlockNumber][domainBlockNumber];

        for (int i = 0; i < domainBlockNumber; i++) {
            for (int j = 0; j < domainBlockNumber; j++) {
                RGBBlock reducedBlock = domainBlocks[i][j].reduce(RBD, this.reductionStrategy);
                reducedDomainBlocksPair[i][j] = new RGBReducedPair(domainBlocks[i][j], reducedBlock);
            }
        }

        List<RGBRangeMatch> rangeMatches = new ArrayList<>();

        for (RGBBlock[] rangesRow : rangeBlocks) {
            for (RGBBlock range : rangesRow) {
                RGBCompressedBlock bestDomain = findBestDomainMatch(range, reducedDomainBlocksPair);
                RGBRangeMatch rangeMatch = new RGBRangeMatch(range, bestDomain);

                rangeMatches.add(rangeMatch);
            }

        }

        System.out.println("Compression Finished!");
        return rangeMatches;

    }

}
