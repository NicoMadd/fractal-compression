package implementations.java.compression.src.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import implementations.java.compression.src.algorithms.concurrent.BlockBuilder;
import implementations.java.compression.src.algorithms.concurrent.DomainFinder;
import implementations.java.compression.src.algorithms.concurrent.ReducedDomainPairBuilder;
import implementations.java.compression.src.pipelines.RunLogging;
import implementations.java.compression.src.utils.fractal.block.GrayBlock;
import implementations.java.compression.src.utils.fractal.block.reductions.BicubicReductionStrategy;
import implementations.java.compression.src.utils.fractal.block.reductions.MeanReductionStrategy;
import implementations.java.compression.src.utils.fractal.block.reductions.ReductionStrategy;
import implementations.java.compression.src.utils.fractal.mapping.FractalMapping;
import implementations.java.compression.src.utils.fractal.reducedpair.GrayReducedPair;
import implementations.java.compression.src.utils.image.ImageMetadata;
import implementations.java.compression.src.utils.image.pixel.GrayPixel;

public class GrayBlockCompression {

    private GrayPixel[][] imagePixels;
    private int imageWidth;
    private int imageHeight;

    private GrayBlock[][] rangeBlocks;
    private GrayBlock[][] domainBlocks;

    // RANGE BLOCK DIMENSION
    private int RBD;

    // DOMAIN BLOCK DIMENSION
    private int DBD;

    // Reduction strategy to reduce blocks
    private ReductionStrategy reductionStrategy;

    // number of threads to use for compression.
    private int parallelism;

    public GrayBlockCompression(int rbd, int dbd, ReductionStrategy reductionStrategy, int parallelism) {
        this.RBD = rbd;
        this.DBD = dbd;
        this.reductionStrategy = reductionStrategy;
        this.parallelism = parallelism;
    }

    public int getRangeSize() {
        return this.RBD;
    }

    public int getDomainSize() {
        return this.DBD;
    }

    public ReductionStrategy getReductionStrategy() {
        return this.reductionStrategy;
    }

    private String getReductionStrategyDescription() {
        if (reductionStrategy instanceof BicubicReductionStrategy) {
            return "bicubic-reducing";
        }
        if (reductionStrategy instanceof MeanReductionStrategy) {
            return "mean-reducing";
        }
        return "reducing";
    }

    private GrayBlock[][] buildRangeBlocks(int rangeBlocksNumber) throws InterruptedException, ExecutionException {

        ExecutorService es = Executors.newFixedThreadPool(this.parallelism);

        List<Callable<GrayBlock>> callables = new ArrayList<>();

        for (int i = 0; i < rangeBlocksNumber; i++) {
            for (int j = 0; j < rangeBlocksNumber; j++) {
                BlockBuilder bb = new BlockBuilder(imagePixels, RBD * i, RBD * j, RBD);
                callables.add(bb);
            }
        }

        GrayBlock[][] blocks = new GrayBlock[rangeBlocksNumber][rangeBlocksNumber];

        try {
            for (Future<GrayBlock> f : es.invokeAll(callables)) {
                GrayBlock gb = f.get();
                blocks[gb.x() / RBD][gb.y() / RBD] = gb;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            es.close();
        }

        return blocks;
    }

    private GrayBlock[][] buildDomainBlocks(int domainBlockNumber) {

        ExecutorService es = Executors.newFixedThreadPool(this.parallelism);

        List<Callable<GrayBlock>> callables = new ArrayList<>();

        for (int i = 0; i < domainBlockNumber; i++) {
            for (int j = 0; j < domainBlockNumber; j++) {
                BlockBuilder bb = new BlockBuilder(imagePixels, DBD * i, DBD * j, DBD);
                callables.add(bb);
            }
        }

        GrayBlock[][] blocks = new GrayBlock[domainBlockNumber][domainBlockNumber];

        try {
            for (Future<GrayBlock> f : es.invokeAll(callables)) {
                GrayBlock gb = f.get();
                blocks[gb.x() / DBD][gb.y() / DBD] = gb;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            es.close();
        }
        return blocks;
    }

    private GrayReducedPair[][] buildReducedDomainPairs(int domainBlockNumber) {

        ExecutorService es = Executors.newFixedThreadPool(this.parallelism);

        List<Callable<GrayReducedPair>> callables = new ArrayList<>();

        for (int i = 0; i < domainBlockNumber; i++) {
            for (int j = 0; j < domainBlockNumber; j++) {
                callables.add(new ReducedDomainPairBuilder(domainBlocks[i][j], RBD, reductionStrategy));
            }
        }

        GrayReducedPair[][] reducedDomainBlocksPair = new GrayReducedPair[domainBlockNumber][domainBlockNumber];

        try {
            for (Future<GrayReducedPair> f : es.invokeAll(callables)) {
                GrayReducedPair pair = f.get();
                GrayBlock d = pair.domain();
                reducedDomainBlocksPair[d.x() / DBD][d.y() / DBD] = pair;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            es.close();
        }

        return reducedDomainBlocksPair;
    }

    private List<FractalMapping> buildFractalMappings(GrayReducedPair[][] reducedDomainBlocksPair) {
        List<FractalMapping> mappings = new ArrayList<>();

        int rb = rangeBlocks.length;
        int totalRanges = rb * rb;
        AtomicInteger counter = new AtomicInteger(0);

        ExecutorService es = Executors.newFixedThreadPool(this.parallelism);

        List<DomainFinder> callables = new ArrayList<>();

        for (GrayBlock[] rangesRow : rangeBlocks) {
            for (GrayBlock range : rangesRow) {
                DomainFinder df = new DomainFinder(reducedDomainBlocksPair, range, counter, totalRanges);
                callables.add(df);
            }
        }

        try {
            for (Future<FractalMapping> f : es.invokeAll(callables)) {
                FractalMapping fm = f.get();
                mappings.add(fm);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            es.close();
        }
        return mappings;
    }

    public List<FractalMapping> compress(ImageMetadata<GrayPixel> metadata)
            throws InterruptedException, ExecutionException {

        this.imagePixels = metadata.getPixels();
        this.imageWidth = metadata.getWidth();
        this.imageHeight = metadata.getHeight();

        int rangeBlocksNumber = this.imageWidth / RBD; // 256 / 4 = 64
        int domainBlockNumber = this.imageHeight / DBD; // 256 / (4 * 2) = 32

        RunLogging.debug("Compress: image " + imageWidth + "x" + imageHeight + ", range " + RBD + ", domain "
                + DBD + " → " + rangeBlocksNumber + "x" + rangeBlocksNumber + " ranges, " + domainBlockNumber + "x"
                + domainBlockNumber + " domains");

        RunLogging.debug("Compress: building range blocks…");
        this.rangeBlocks = buildRangeBlocks(rangeBlocksNumber);
        RunLogging.debug("Compress: building domain blocks…");
        this.domainBlocks = buildDomainBlocks(domainBlockNumber);
        RunLogging.debug("Compress: " + getReductionStrategyDescription() + " domain blocks ("
                + domainBlockNumber * domainBlockNumber + " blocks)…");
        GrayReducedPair[][] reducedDomainBlocksPair = buildReducedDomainPairs(domainBlockNumber);
        RunLogging.debug("Compress: searching best domain + transform per range block…");

        List<FractalMapping> fractalMappings = buildFractalMappings(reducedDomainBlocksPair);
        RunLogging.debug("Compression Finished!");
        return fractalMappings;
    }

}
