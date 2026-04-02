package implementations.java.compression.src.pipelines;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import implementations.java.compression.src.algorithms.GrayBlockCompression;
import implementations.java.compression.src.benchmarks.Iteration;
import implementations.java.compression.src.benchmarks.SimpleCompressionBenchmark;
import implementations.java.compression.src.utils.errors.ErrorUtils;
import implementations.java.compression.src.utils.files.FileUtils;
import implementations.java.compression.src.utils.fractal.block.GrayBlock;
import implementations.java.compression.src.utils.fractal.mapping.Codebook;
import implementations.java.compression.src.utils.fractal.mapping.FractalMapping;
import implementations.java.compression.src.utils.image.pgm.PGMAImageMetadata;
import implementations.java.compression.src.utils.image.pgm.PGMAUtils;
import implementations.java.compression.src.utils.image.pixel.GrayPixel;
import implementations.java.compression.src.utils.image.pixel.utils.GrayPixelUtils;
import implementations.java.compression.src.utils.matrix.MatrixUtils;

public class PGMAPipeline extends Pipeline {

    private PGMAImageMetadata metadata;
    private GrayBlockCompression gbc;

    public PGMAPipeline(PipelineParams params) throws IOException {
        super(params);
        this.gbc = new GrayBlockCompression(params.rangeSize(), params.domainSize());
    }

    protected void init(FileInputStream fis) throws IOException {
        this.metadata = new PGMAImageMetadata(fis);
    }

    public void run(PipelineParams params)
            throws Exception {
        String originalImagePath = params.imagePath();
        int iterations = params.iterations();
        Path runDir = params.runDir();
        Path iterationsDir = FileUtils.iterationsDir(runDir);

        long t0 = System.nanoTime();

        Path codebookPath = runDir.resolve("codebook.fc");

        System.out.println("Looking for file in " + codebookPath.toString());

        Codebook cb = null;
        List<FractalMapping> mappings;

        boolean codebookExists = Files.exists(codebookPath);

        if (codebookExists) {
            System.out.println("Codebook found!");
        } else {
            System.out.println("Codebook not found!");
        }

        if (!params.cleanCodebook() && codebookExists) {
            System.out.println("Reading codebook");
            cb = new Codebook(codebookPath.toString());
            mappings = cb.getMappings();
        } else {
            System.out.println("Processing codebook");
            mappings = this.gbc.compress(metadata);
            cb = new Codebook(params.rangeSize(), params.domainSize(), mappings);
            cb.save(codebookPath.toString());
            System.out.println("Codebook saved to " + codebookPath.toString());
        }

        long t1 = System.nanoTime();

        System.out.println("Starting Decompression");

        // declare decode images
        GrayPixel[][] img = new GrayPixel[metadata.getWidth()][metadata.getHeight()];
        GrayPixel[][] next = new GrayPixel[metadata.getWidth()][metadata.getHeight()];

        // fill random values
        MatrixUtils.fill(img, () -> GrayPixelUtils.getRandomPixel());
        MatrixUtils.fill(next, () -> GrayPixelUtils.getRandomPixel());

        PGMAUtils.saveToImage(img, FileUtils.inRunDir(iterationsDir, "initial").toString());

        System.out.println("Iterating over " + iterations + " iterations");

        SimpleCompressionBenchmark scb = new SimpleCompressionBenchmark();

        // N iterations
        for (int iter = 0; iter <= iterations; iter++) {

            long iterationStartTs = System.nanoTime();

            System.out.println("Iteration " + iter);

            // loop compressed blocks
            for (FractalMapping fm : mappings) {

                int xDomain = fm.domainX();
                int yDomain = fm.domainY();

                GrayPixel[][] domainPixels = new GrayPixel[gbc.getDomainSize()][gbc.getDomainSize()];

                for (int i = 0; i < gbc.getDomainSize(); i++) {
                    for (int j = 0; j < gbc.getDomainSize(); j++) {
                        domainPixels[i][j] = img[xDomain + i][yDomain + j];
                    }
                }

                GrayBlock domainBlock = new GrayBlock(xDomain, yDomain, domainPixels);

                GrayBlock reducedBlock = domainBlock.reduce(gbc.getRangeSize());

                GrayPixel[][] newPixels = new GrayPixel[gbc.getRangeSize()][gbc.getRangeSize()];

                for (int i = 0; i < gbc.getRangeSize(); i++) {
                    for (int j = 0; j < gbc.getRangeSize(); j++) {
                        GrayPixel p = reducedBlock.pixels()[i][j];

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

                for (int i = 0; i < gbc.getRangeSize(); i++) {
                    for (int j = 0; j < gbc.getRangeSize(); j++) {
                        next[xRangeOffset + i][yRangeOffset + j] = newPixels[i][j];
                    }
                }
            }

            long iterationEndTs = System.nanoTime();

            Path iterBase = FileUtils.inRunDir(iterationsDir, "iter_" + iter);
            String currentIterationPath = iterBase + ".pgm";
            PGMAUtils.saveToImage(next, iterBase.toString());

            double mse = PGMAUtils.calculateMSE(originalImagePath, currentIterationPath);
            System.out.println("MSE: " + mse);

            double psnr = ErrorUtils.calculatePSNR(mse);
            System.out.println("PSNR: " + psnr);
            Iteration iteration = new Iteration(iter, iterationEndTs, iterationEndTs - iterationStartTs, mse, psnr);
            scb.add(iteration);

            // Jacobi: keep two buffers; swap references so next pass reads the image
            // we just wrote and writes into the other buffer.
            GrayPixel[][] tmp = img;
            img = next;
            next = tmp;

        }

        long t2 = System.nanoTime();

        long compressionElapsedNanos = t1 - t0;
        double compressionSeconds = compressionElapsedNanos / 1_000_000_000.0;

        long decompressionElapsedNanos = t2 - t1;
        double decompressionSeconds = decompressionElapsedNanos / 1_000_000_000.0;

        System.out.println("Compression took: " + compressionSeconds + "s");
        System.out.println("Decompression took: " + decompressionSeconds + "s");

        scb.saveTo(iterationsDir.toString());

        calculateCompressionRatio(originalImagePath, codebookPath.toString());
    }

    private void calculateCompressionRatio(String originalImagePath, String codebookPath) throws IOException {
        long originalSize = FileUtils.getFileSize(originalImagePath);
        long codebookSize = FileUtils.getFileSize(codebookPath);

        double n = (double) originalSize / codebookSize;

        String ratio = String.format("%.2f:1", n);

        System.out.println("Compression ratio: " + ratio);

    }
}
