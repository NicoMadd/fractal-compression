package implementations.java.compression.src.pipelines;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import implementations.java.compression.src.algorithms.GrayBlockCompression;
import implementations.java.compression.src.benchmarks.BenchmarkRegistry;
import implementations.java.compression.src.benchmarks.HostEnvironmentMetrics;
import implementations.java.compression.src.benchmarks.ImageErrorMetrics;
import implementations.java.compression.src.benchmarks.Iteration;
import implementations.java.compression.src.benchmarks.MemorySampler;
import implementations.java.compression.src.benchmarks.RunManifest;
import implementations.java.compression.src.benchmarks.RunnerInfo;
import implementations.java.compression.src.benchmarks.SimpleCompressionBenchmark;
import implementations.java.compression.src.utils.baselines.CompressionBaselines;
import implementations.java.compression.src.utils.errors.ErrorUtils;
import implementations.java.compression.src.utils.files.FileUtils;
import implementations.java.compression.src.utils.fractal.block.GrayBlock;
import implementations.java.compression.src.utils.fractal.mapping.Codebook;
import implementations.java.compression.src.utils.fractal.mapping.FractalMapping;
import implementations.java.compression.src.utils.fractal.transformation.Transformation;
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
        this.gbc = new GrayBlockCompression(params.rangeSize(), params.domainSize(), params.reductionStrategy());
    }

    protected void init(FileInputStream fis) throws IOException {
        this.metadata = new PGMAImageMetadata(fis);
    }

    public void run(PipelineParams params) throws Exception {
        String originalImagePath = params.imagePath();
        int iterations = params.iterations();
        Path runDir = params.runDir();
        Path iterationsDir = FileUtils.iterationsDir(runDir);
        MemorySampler memorySampler = new MemorySampler();
        memorySampler.sample();

        long t0 = System.nanoTime();

        Path codebookPath = Codebook.pathForGeometry(runDir, params.rangeSize(), params.domainSize());

        System.out.println("Looking for codebook at " + codebookPath);

        List<FractalMapping> mappings = null;

        if (!params.cleanCodebook() && Files.exists(codebookPath)) {
            Codebook loaded = new Codebook(codebookPath.toString());
            if (loaded.rangeSize() == params.rangeSize() && loaded.domainSize() == params.domainSize()) {
                System.out.println("Reading codebook from " + codebookPath);
                mappings = loaded.getMappings();
            } else {
                System.out.println("On-disk codebook range/domain does not match this run; rebuilding.");
            }
        }

        if (mappings == null) {
            System.out.println("Processing codebook");
            mappings = this.gbc.compress(metadata);
            Codebook cb = new Codebook(params.rangeSize(), params.domainSize(), mappings);
            cb.save(codebookPath.toString());
            System.out.println("Codebook saved to " + codebookPath);
        }

        long t1 = System.nanoTime();
        memorySampler.sample();

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

        double finalMse = 0;
        double finalMae = 0;
        double finalPsnr = 0;

        // N iterations
        for (int iter = 0; iter <= iterations; iter++) {

            long iterationStartTs = System.nanoTime();

            System.out.println("Iteration " + iter);

            int mapCount = mappings.size();
            int mapStep = Math.max(1, mapCount / 25);
            // loop compressed blocks
            for (int mi = 0; mi < mapCount; mi++) {
                FractalMapping fm = mappings.get(mi);
                if (mapCount >= 800 && (mi == 0 || mi + 1 == mapCount || (mi + 1) % mapStep == 0)) {
                    System.out.println("  applying mappings " + (mi + 1) + "/" + mapCount);
                }

                int xDomain = fm.domainX();
                int yDomain = fm.domainY();

                GrayPixel[][] domainPixels = new GrayPixel[gbc.getDomainSize()][gbc.getDomainSize()];

                for (int i = 0; i < gbc.getDomainSize(); i++) {
                    for (int j = 0; j < gbc.getDomainSize(); j++) {
                        domainPixels[i][j] = img[xDomain + i][yDomain + j];
                    }
                }

                GrayBlock domainBlock = new GrayBlock(xDomain, yDomain, domainPixels);

                GrayBlock reducedBlock = domainBlock.reduce(gbc.getRangeSize(), gbc.getReductionStrategy());

                GrayPixel[][] newPixels = new GrayPixel[gbc.getRangeSize()][gbc.getRangeSize()];

                GrayPixel[][] transformedPixels = new GrayPixel[gbc.getRangeSize()][gbc.getRangeSize()];

                Transformation t = fm.transformation();
                t.transform(reducedBlock.pixels(), transformedPixels);

                for (int i = 0; i < gbc.getRangeSize(); i++) {
                    for (int j = 0; j < gbc.getRangeSize(); j++) {
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

            ImageErrorMetrics err = PGMAUtils.calculateErrorMetrics(originalImagePath, currentIterationPath);
            double mse = err.mse();
            double mae = err.mae();
            System.out.println("MSE: " + mse);
            System.out.println("MAE: " + mae);

            double psnr = ErrorUtils.calculatePSNR(mse);
            System.out.println("PSNR: " + psnr);
            Iteration iteration = new Iteration(iter, iterationEndTs, iterationEndTs - iterationStartTs, mse, mae,
                    psnr);
            scb.add(iteration);

            finalMse = mse;
            finalMae = mae;
            finalPsnr = psnr;

            memorySampler.sample();

            // Jacobi: keep two buffers; swap references so next pass reads the image
            // we just wrote and writes into the other buffer.
            GrayPixel[][] tmp = img;
            img = next;
            next = tmp;

        }

        long t2 = System.nanoTime();
        memorySampler.sample();

        long compressionElapsedNanos = t1 - t0;
        double compressionSeconds = compressionElapsedNanos / 1_000_000_000.0;

        long decompressionElapsedNanos = t2 - t1;
        double decompressionSeconds = decompressionElapsedNanos / 1_000_000_000.0;

        System.out.println("Compression took: " + compressionSeconds + "s");
        System.out.println("Decompression took: " + decompressionSeconds + "s");

        scb.saveTo(iterationsDir.toString());

        calculateCompressionRatio(originalImagePath, codebookPath.toString());

        writeBenchmarkOutputs(params, originalImagePath, runDir, iterationsDir, codebookPath, iterations,
                compressionSeconds, decompressionSeconds, finalMse, finalMae, finalPsnr, memorySampler);
    }

    // Baseline PNG/zip, run_manifest.json, registry line
    private void writeBenchmarkOutputs(
            PipelineParams params,
            String originalImagePath,
            Path runDir,
            Path iterationsDir,
            Path codebookPath,
            int iterations,
            double compressionSeconds,
            double decompressionSeconds,
            double finalMse,
            double finalMae,
            double finalPsnr,
            MemorySampler memorySampler) throws IOException {

        Path originalCopyPath = runDir.resolve("original.pgm");
        Path finalIterPath = iterationsDir.resolve("iter_" + iterations + ".pgm");
        Path baselinesDir = runDir.resolve("baselines");

        long bytesOriginalInput = FileUtils.getFileSize(originalImagePath);
        long bytesOriginalCopy = FileUtils.getFileSize(originalCopyPath);
        long bytesCodebook = FileUtils.getFileSize(codebookPath.toString());
        long pngOrig = CompressionBaselines.writePngFromPgm(originalCopyPath, baselinesDir.resolve("original.png"));
        long pngFinal = CompressionBaselines.writePngFromPgm(finalIterPath,
                baselinesDir.resolve("reconstruction.png"));
        long zipOrig = CompressionBaselines.writeZipDeflatedArchive(originalCopyPath,
                baselinesDir.resolve("original_deflated.zip"), "original.pgm");
        String zipEntryName = codebookPath.getFileName().toString();
        long zipCb = CompressionBaselines.writeZipDeflatedArchive(codebookPath,
                baselinesDir.resolve("codebook_deflated.zip"), zipEntryName);
        double ratioOc = (double) bytesOriginalInput / (double) bytesCodebook;

        RunManifest manifest = new RunManifest(
                System.currentTimeMillis(),
                RunManifest.gitShaFromEnv(),
                originalImagePath,
                runDir.toAbsolutePath().normalize().toString(),
                iterations,
                params.rangeSize(),
                params.domainSize(),
                params.cleanCodebook(),
                compressionSeconds,
                decompressionSeconds,
                bytesOriginalInput,
                bytesOriginalCopy,
                bytesCodebook,
                pngOrig,
                pngFinal,
                zipOrig,
                zipCb,
                ratioOc,
                finalMse,
                finalMae,
                finalPsnr,
                memorySampler.peakHeapUsedBytes(),
                memorySampler.peakRuntimeUsedBytes(),
                memorySampler.avgHeapUsedBytes(),
                memorySampler.avgRuntimeUsedBytes(),
                RunnerInfo.javaDefault(),
                HostEnvironmentMetrics.capture());
        manifest.write(runDir);
        BenchmarkRegistry.appendLine(manifest.toJsonLine());

        System.out.println("Wrote baseline PNG/zip under: " + baselinesDir.toAbsolutePath().normalize());
        System.out.println("Wrote run manifest: " + runDir.resolve("run_manifest.json"));
        System.out.println("Appended run to registry: " + FileUtils.benchmarkRegistryPath());
    }

    private void calculateCompressionRatio(String originalImagePath, String codebookPath) throws IOException {
        long originalSize = FileUtils.getFileSize(originalImagePath);
        long codebookSize = FileUtils.getFileSize(codebookPath);

        double n = (double) originalSize / codebookSize;

        String ratio = String.format("%.2f:1", n);

        System.out.println("Compression ratio: " + ratio);

    }
}
