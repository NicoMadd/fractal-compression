package implementations.java.compression.src.pipelines;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import implementations.java.compression.src.algorithms.squared.GraySquaredBlockCompression;
import implementations.java.compression.src.benchmarks.BenchmarkRegistry;
import implementations.java.compression.src.benchmarks.HostEnvironmentMetrics;
import implementations.java.compression.src.benchmarks.ImageErrorMetrics;
import implementations.java.compression.src.benchmarks.Iteration;
import implementations.java.compression.src.benchmarks.RunManifest;
import implementations.java.compression.src.benchmarks.RunnerInfo;
import implementations.java.compression.src.benchmarks.SimpleCompressionBenchmark;
import implementations.java.compression.src.pipelines.concurrent.Decompressor;
import implementations.java.compression.src.utils.baselines.CompressionBaselines;
import implementations.java.compression.src.utils.errors.ErrorUtils;
import implementations.java.compression.src.utils.files.FileUtils;
import implementations.java.compression.src.utils.fractal.mapping.Codebook;
import implementations.java.compression.src.utils.fractal.mapping.FractalMapping;
import implementations.java.compression.src.utils.image.pgm.PGMAImageMetadata;
import implementations.java.compression.src.utils.image.pgm.PGMAUtils;
import implementations.java.compression.src.utils.image.pixel.GrayPixel;
import implementations.java.compression.src.utils.image.pixel.utils.GrayPixelUtils;
import implementations.java.compression.src.utils.matrix.MatrixUtils;

public class PGMAPipeline extends Pipeline {

    private PGMAImageMetadata metadata;
    private GraySquaredBlockCompression gbc;

    public PGMAPipeline(PipelineParams params) throws IOException {
        super(params);
        this.gbc = new GraySquaredBlockCompression(params.rangeSize(), params.domainSize(), params.reductionStrategy(),
                params.compressionParallelism());
    }

    protected void init(FileInputStream fis) throws IOException {
        this.metadata = new PGMAImageMetadata(fis);
    }

    public void run(PipelineParams params) throws Exception {
        String originalImagePath = params.imagePath();
        int iterations = params.iterations();
        Path runDir = params.runDir();
        Path iterationsDir = FileUtils.iterationsDir(runDir);
        beginRunMemorySampling();

        if (params.skipCompression() && params.skipDecompression()) {
            throw new IllegalStateException("Cannot skip both compression and decompression.");
        }

        long t0 = System.nanoTime();

        Path codebookPath = Codebook.pathForGeometry(runDir, params.rangeSize(), params.domainSize());

        List<FractalMapping> mappings = null;

        if (params.skipCompression()) {
            System.out.println("Skipping compression (--skip-compression); loading codebook from disk.");
            if (!Files.exists(codebookPath)) {
                throw new IllegalStateException(
                        "Missing codebook for --skip-compression: " + codebookPath
                                + " (run without the flag to build).");
            }
            Codebook loaded = new Codebook(codebookPath.toString());
            if (loaded.rangeSize() != params.rangeSize() || loaded.domainSize() != params.domainSize()) {
                throw new IllegalStateException("On-disk codebook range/domain does not match this run's -r/-d.");
            }
            System.out.println("Reading codebook from " + codebookPath);
            mappings = loaded.getMappings();
        } else {
            System.out.println("Looking for codebook at " + codebookPath);

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
        }

        long t1 = System.nanoTime();
        memorySampler.sample();

        double compressionSeconds = params.skipCompression() ? 0.0 : (t1 - t0) / 1_000_000_000.0;

        SimpleCompressionBenchmark scb = new SimpleCompressionBenchmark();
        IterationOutMetrics iterationOutMetrics = new IterationOutMetrics();

        double decompressionSeconds;
        double decompressionDecodeSeconds;
        double decompressionSaveSeconds;
        double decompressionSnapshotMetricsSeconds;
        double decodeIterationAvgSeconds;
        double decodeIterationMinSeconds;
        double decodeIterationMaxSeconds;
        long t2;
        ExecutorService es = null;

        if (params.skipDecompression()) {
            System.out.println("Skipping decompression (--skip-decompression); no decode iterations or error metrics.");
            iterationOutMetrics.setMse(Double.NaN);
            iterationOutMetrics.setMae(Double.NaN);
            iterationOutMetrics.setPsnr(Double.NaN);
            decompressionSeconds = 0.0;
            decompressionDecodeSeconds = 0.0;
            decompressionSaveSeconds = 0.0;
            decompressionSnapshotMetricsSeconds = 0.0;
            decodeIterationAvgSeconds = 0.0;
            decodeIterationMinSeconds = 0.0;
            decodeIterationMaxSeconds = 0.0;
            scb.saveTo(iterationsDir.toString());
            t2 = System.nanoTime();
        } else {

            System.out.println("Starting Decompression");
            if (params.skipIterationSaves()) {
                System.out.println(
                        "Per-iteration PGM saves disabled (--no-iter-save); MSE/MAE/PSNR computed once from final frame.");
            }

            // declare decode images
            GrayPixel[][] img = new GrayPixel[metadata.getWidth()][metadata.getHeight()];
            GrayPixel[][] next = new GrayPixel[metadata.getWidth()][metadata.getHeight()];

            // fill random values
            MatrixUtils.fill(img, () -> GrayPixelUtils.getRandomPixel());
            MatrixUtils.fill(next, () -> GrayPixelUtils.getRandomPixel());

            long totalDecodeNanos = 0;
            long minDecodeNanos = Long.MAX_VALUE;
            long maxDecodeNanos = Long.MIN_VALUE;
            int decodePassCount = 0;
            long totalSaveNanos = 0;
            long totalSnapshotMetricsNanos = 0;

            long saveStart = System.nanoTime();
            PGMAUtils.saveToImage(img, FileUtils.inRunDir(iterationsDir, "initial").toString());
            totalSaveNanos += System.nanoTime() - saveStart;

            System.out.println("Iterating over " + iterations + " iterations");

            es = Executors.newFixedThreadPool(params.decompressionParallelism());

            // N iterations
            for (int iter = 0; iter <= iterations; iter++) {

                long iterationStartTs = System.nanoTime();

                CountDownLatch latch = new CountDownLatch(mappings.size());

                System.out.println("Iteration " + iter);

                int mapCount = mappings.size();
                // loop compressed blocks

                for (int mi = 0; mi < mapCount; mi++) {
                    FractalMapping fm = mappings.get(mi);

                    Decompressor decompressor = new Decompressor(fm, gbc.getDomainSize(), gbc.getRangeSize(),
                            gbc.getReductionStrategy(), img, next, latch);

                    es.submit(decompressor);
                }

                latch.await();

                long iterationEndTs = System.nanoTime();

                long decodeSliceNanos = iterationEndTs - iterationStartTs;
                totalDecodeNanos += decodeSliceNanos;
                minDecodeNanos = Math.min(minDecodeNanos, decodeSliceNanos);
                maxDecodeNanos = Math.max(maxDecodeNanos, decodeSliceNanos);
                decodePassCount++;

                Path iterBase = FileUtils.inRunDir(iterationsDir, "iter_" + iter);
                String currentIterationPath = iterBase + ".pgm";

                boolean saveIterationFrames = !params.skipIterationSaves();
                if (saveIterationFrames) {
                    saveStart = System.nanoTime();
                    PGMAUtils.saveToImage(next, iterBase.toString());
                    totalSaveNanos += System.nanoTime() - saveStart;
                }

                long metricsStart = System.nanoTime();
                if (saveIterationFrames) {
                    recordIterationErrorMetrics(originalImagePath, currentIterationPath, iter, iterationStartTs,
                            iterationEndTs, scb, iterationOutMetrics);
                } else {
                    scb.add(new Iteration(iter, iterationEndTs, iterationEndTs - iterationStartTs, Double.NaN,
                            Double.NaN,
                            Double.NaN));
                }
                memorySampler.sample();
                totalSnapshotMetricsNanos += System.nanoTime() - metricsStart;

                // Jacobi: keep two buffers; swap references so next pass reads the image
                // we just wrote and writes into the other buffer.
                GrayPixel[][] tmp = img;
                img = next;
                next = tmp;

            }

            if (params.skipIterationSaves()) {
                saveStart = System.nanoTime();
                PGMAUtils.saveToImage(img, FileUtils.inRunDir(iterationsDir, "iter_" + iterations).toString());
                totalSaveNanos += System.nanoTime() - saveStart;

                long metricsFinalStart = System.nanoTime();
                Path finalIterPgm = iterationsDir.resolve("iter_" + iterations + ".pgm");
                recordFinalIterationErrorMetrics(originalImagePath, finalIterPgm.toString(), scb, iterationOutMetrics);
                totalSnapshotMetricsNanos += System.nanoTime() - metricsFinalStart;
            }

            t2 = System.nanoTime();
            memorySampler.sample();

            long decompressionElapsedNanos = t2 - t1;
            decompressionSeconds = decompressionElapsedNanos / 1_000_000_000.0;
            decompressionDecodeSeconds = totalDecodeNanos / 1_000_000_000.0;
            decompressionSaveSeconds = totalSaveNanos / 1_000_000_000.0;
            decompressionSnapshotMetricsSeconds = totalSnapshotMetricsNanos / 1_000_000_000.0;
            decodeIterationAvgSeconds = decodePassCount > 0
                    ? (totalDecodeNanos / (double) decodePassCount) / 1_000_000_000.0
                    : 0.0;
            decodeIterationMinSeconds = decodePassCount > 0 ? minDecodeNanos / 1_000_000_000.0 : 0.0;
            decodeIterationMaxSeconds = decodePassCount > 0 ? maxDecodeNanos / 1_000_000_000.0 : 0.0;

            System.out.println("Decompression took: " + decompressionSeconds + "s (decode " + decompressionDecodeSeconds
                    + "s, save " + decompressionSaveSeconds + "s, metrics+sample " + decompressionSnapshotMetricsSeconds
                    + "s)");
            System.out
                    .println("Per-pass decode: avg " + decodeIterationAvgSeconds + "s, min " + decodeIterationMinSeconds
                            + "s, max " + decodeIterationMaxSeconds + "s (" + decodePassCount + " passes)");

            scb.saveTo(iterationsDir.toString());

        }

        System.out.println("Compression took: " + compressionSeconds + "s"
                + (params.skipCompression() ? " (--skip-compression; phase not timed)" : ""));
        if (params.skipDecompression()) {
            System.out.println("Decompression skipped (--skip-decompression); decode timings are 0 in the manifest.");
        }

        calculateCompressionRatio(originalImagePath, codebookPath.toString());

        writeBenchmarkOutputs(params, originalImagePath, runDir, iterationsDir, codebookPath, iterations,
                compressionSeconds, decompressionSeconds, decompressionDecodeSeconds, decompressionSaveSeconds,
                decompressionSnapshotMetricsSeconds, decodeIterationAvgSeconds, decodeIterationMinSeconds,
                decodeIterationMaxSeconds,                 iterationOutMetrics.getMse(), iterationOutMetrics.getMae(),
                iterationOutMetrics.getPsnr());

        if (es != null) {
            es.shutdown();
        }
    }

    private void recordIterationErrorMetrics(String originalImagePath,
            String currentIterationPath,
            int iter,
            long iterationStartTs,
            long iterationEndTs,
            SimpleCompressionBenchmark scb,
            IterationOutMetrics outMetrics) throws IOException {
        ImageErrorMetrics err = PGMAUtils.calculateErrorMetrics(originalImagePath, currentIterationPath);
        double mse = err.mse();
        double mae = err.mae();
        System.out.println("MSE: " + mse);
        System.out.println("MAE: " + mae);

        double psnr = ErrorUtils.calculatePSNR(mse);
        System.out.println("PSNR: " + psnr);
        scb.add(new Iteration(iter, iterationEndTs, iterationEndTs - iterationStartTs, mse, mae, psnr));

        outMetrics.setMse(mse);
        outMetrics.setMae(mae);
        outMetrics.setPsnr(psnr);
    }

    private void recordFinalIterationErrorMetrics(String originalImagePath,
            String finalIterationPath,
            SimpleCompressionBenchmark scb,
            IterationOutMetrics outMetrics) throws IOException {
        ImageErrorMetrics err = PGMAUtils.calculateErrorMetrics(originalImagePath, finalIterationPath);
        double mse = err.mse();
        double mae = err.mae();
        System.out.println("MSE: " + mse);
        System.out.println("MAE: " + mae);

        double psnr = ErrorUtils.calculatePSNR(mse);
        System.out.println("PSNR: " + psnr);

        outMetrics.setMse(mse);
        outMetrics.setMae(mae);
        outMetrics.setPsnr(psnr);
        scb.replaceLastIterationError(mse, mae, psnr);
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
            double decompressionDecodeSeconds,
            double decompressionSaveSeconds,
            double decompressionSnapshotMetricsSeconds,
            double decodeIterationAvgSeconds,
            double decodeIterationMinSeconds,
            double decodeIterationMaxSeconds,
            double finalMse,
            double finalMae,
            double finalPsnr) throws IOException {

        Path originalCopyPath = runDir.resolve("original.pgm");
        Path finalIterPath = iterationsDir.resolve("iter_" + iterations + ".pgm");
        Path baselinesDir = runDir.resolve("baselines");

        long bytesOriginalInput = FileUtils.getFileSize(originalImagePath);
        long bytesOriginalCopy = FileUtils.getFileSize(originalCopyPath);
        long bytesCodebook = FileUtils.getFileSize(codebookPath.toString());
        long pngOrig = CompressionBaselines.writePngFromPgm(originalCopyPath, baselinesDir.resolve("original.png"));
        long pngFinal;
        if (params.skipDecompression() || !Files.exists(finalIterPath)) {
            pngFinal = 0L;
        } else {
            pngFinal = CompressionBaselines.writePngFromPgm(finalIterPath,
                    baselinesDir.resolve("reconstruction.png"));
        }
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
                params.skipIterationSaves(),
                params.skipCompression(),
                params.skipDecompression(),
                params.compressionParallelism(),
                params.decompressionParallelism(),
                compressionSeconds,
                decompressionSeconds,
                decompressionDecodeSeconds,
                decompressionSaveSeconds,
                decompressionSnapshotMetricsSeconds,
                decodeIterationAvgSeconds,
                decodeIterationMinSeconds,
                decodeIterationMaxSeconds,
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
