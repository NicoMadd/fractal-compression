package implementations.java.compression.src.algorithms;

import java.util.ArrayList;
import java.util.List;

import implementations.java.compression.src.utils.fractal.Block;
import implementations.java.compression.src.utils.fractal.CompressedBlock;
import implementations.java.compression.src.utils.fractal.RangeMatch;
import implementations.java.compression.src.utils.fractal.ReducedPair;
import implementations.java.compression.src.utils.image.pixel.Pixel;
import implementations.java.compression.src.utils.image.pixel.RGB;
import implementations.java.compression.src.utils.image.ppm.PPMImageMetadata;
import implementations.java.compression.src.utils.matrix.MatrixUtils;

public class BlockCompression {

    private Pixel[][] imagePixels;
    private int imageWidth;
    private int imageHeight;

    private Block[][] rangeBlocks;
    private Block[][] domainBlocks;

    private float[] meansR;

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
    private float calculateS(List<Pixel> rangePixels, List<Pixel> rdPixels, float[] meansD, RGB c) {
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

    private CompressedBlock findBestDomainMatch(Block range, ReducedPair[][] reducedDomainsPairs) {

        this.meansR = range.means();

        CompressedBlock bestCompression = null;
        float bestError = Float.MAX_VALUE;

        for (ReducedPair[] row : reducedDomainsPairs) {
            for (ReducedPair rdp : row) {
                Block rd = rdp.reduced();
                float[] meansD = rd.means();

                List<Pixel> rangePixels = MatrixUtils.flatMap(range.pixels());
                List<Pixel> rdPixels = MatrixUtils.flatMap(rd.pixels());

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
                    bestCompression = new CompressedBlock(range, rdp.domain(), sArr, oArr);
                    bestError = blockError;
                }
            }
        }

        return bestCompression;

    }

    public List<RangeMatch> compress(PPMImageMetadata metadata) {

        this.imagePixels = metadata.getPixels();
        this.imageWidth = metadata.getWidth();
        this.imageHeight = metadata.getHeight();

        int rangeBlocksNumber = this.imageWidth / RBD; // 256 / 4 = 64
        int domainBlockNumber = this.imageHeight / DBD; // 256 / (4 * 2) = 32

        // create 4x4 blocks - range blocks - BxB blocks
        this.rangeBlocks = new Block[rangeBlocksNumber][rangeBlocksNumber];

        for (int i = 0; i < rangeBlocksNumber; i++) {
            for (int j = 0; j < rangeBlocksNumber; j++) {
                Pixel[][] blockPixels = new Pixel[RBD][RBD];

                for (int k = 0; k < RBD; k++) {
                    for (int l = 0; l < RBD; l++) {
                        blockPixels[k][l] = this.imagePixels[RBD * i + k][RBD * j + l];
                    }
                }

                Block block = new Block(4 * i, 4 * j, blockPixels);
                rangeBlocks[i][j] = block;
            }
        }

        // create 8x8 blocks - domain blocks - 2Bx2B - DxD
        this.domainBlocks = new Block[domainBlockNumber][domainBlockNumber];

        for (int i = 0; i < domainBlockNumber; i++) {
            for (int j = 0; j < domainBlockNumber; j++) {
                Pixel[][] blockPixels = new Pixel[DBD][DBD];

                for (int k = 0; k < DBD; k++) {
                    for (int l = 0; l < DBD; l++) {

                        blockPixels[k][l] = this.imagePixels[DBD * i + k][DBD * j + l];
                    }
                }

                Block block = new Block(8 * i, 8 * j, blockPixels);
                domainBlocks[i][j] = block;
            }
        }

        // For each domain find the reduced domain block.
        ReducedPair[][] reducedDomainBlocksPair = new ReducedPair[domainBlockNumber][domainBlockNumber];

        for (int i = 0; i < domainBlockNumber; i++) {
            for (int j = 0; j < domainBlockNumber; j++) {
                Block reducedBlock = domainBlocks[i][j].reduce(RBD);
                reducedDomainBlocksPair[i][j] = new ReducedPair(domainBlocks[i][j], reducedBlock);
            }
        }

        List<RangeMatch> rangeMatches = new ArrayList<>();

        for (Block[] rangesRow : rangeBlocks) {
            for (Block range : rangesRow) {
                CompressedBlock bestDomain = findBestDomainMatch(range, reducedDomainBlocksPair);
                RangeMatch rangeMatch = new RangeMatch(range, bestDomain);

                rangeMatches.add(rangeMatch);
            }

        }

        System.out.println("Compression Finished!");
        return rangeMatches;

    }

}
