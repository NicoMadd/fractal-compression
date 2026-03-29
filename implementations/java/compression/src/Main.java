package implementations.java.compression.src;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import implementations.java.compression.src.algorithms.GrayBlockCompression;
import implementations.java.compression.src.algorithms.RGBBlockCompression;
import implementations.java.compression.src.benchmarks.Iteration;
import implementations.java.compression.src.benchmarks.SimpleCompressionBenchmark;
import implementations.java.compression.src.utils.fractal.block.GrayBlock;
import implementations.java.compression.src.utils.fractal.block.compressed.GrayCompressedBlock;
import implementations.java.compression.src.utils.fractal.rangematch.GrayRangeMatch;
import implementations.java.compression.src.utils.image.pgm.PGMAImageMetadata;
import implementations.java.compression.src.utils.image.pgm.PGMAUtils;
import implementations.java.compression.src.utils.image.pixel.GrayPixel;
import implementations.java.compression.src.utils.image.pixel.utils.GrayPixelUtils;
import implementations.java.compression.src.utils.matrix.MatrixUtils;

public class Main {

    private static final Path ITERATIONS_ROOT = Paths.get("").toAbsolutePath().resolve("iterations");

    private static double calculatePSNR(double mse) {
        if (mse == 0)
            return Double.POSITIVE_INFINITY;
        return 10 * Math.log10(Math.pow(255, 2) / mse);
    }

    private static Path inRunDir(Path runDir, String basenameNoExt) {
        return runDir.resolve(basenameNoExt);
    }

    /** Folder name derived from image filename (stem), safe for the filesystem. */
    private static String runFolderName(String imagePath) {
        String fileName = Paths.get(imagePath).getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String stem = dot > 0 ? fileName.substring(0, dot) : fileName;
        String safe = stem.replaceAll("[^a-zA-Z0-9._-]", "_");
        return safe.isEmpty() ? "image" : safe;
    }

    private static void cleanAndCreateRunDir(Path runDir) throws IOException {
        if (Files.isDirectory(runDir)) {
            try (Stream<Path> walk = Files.walk(runDir)) {
                walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            }
        }
        Files.createDirectories(runDir);
    }

    private static void copyOriginal(String imagePath, Path runDir) throws IOException {
        Path dest = runDir.resolve("original.pgm");
        Files.copy(Paths.get(imagePath), dest, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void main(String[] args) throws IOException {

        if (args.length < 2) {
            System.out.println("Usage: <image path> <iterations>");
            return;
        }

        String imagePath = args[0];
        int iterations;
        try {
            iterations = Integer.parseInt(args[1]);
            if (iterations < 0) {
                System.out.println("Iterations must be non-negative.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Iterations must be an integer.");
            return;
        }

        Path runDir = ITERATIONS_ROOT.resolve(runFolderName(imagePath));

        PGMAImageMetadata metadata = null;

        try (FileInputStream fis = new FileInputStream(imagePath)) {
            System.out.println("File Input Stream opened.");
            metadata = new PGMAImageMetadata(fis);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } finally {
            System.out.println("File Input Stream closed.");
        }

        if (metadata == null) {
            return;
        }

        Files.createDirectories(ITERATIONS_ROOT);
        cleanAndCreateRunDir(runDir);
        copyOriginal(imagePath, runDir);
        compressImage(imagePath, metadata, iterations, runDir);

    }

    private static void compressImage(String originalImagePath, PGMAImageMetadata metadata, int iterations,
            Path runDir) throws IOException {

        long t0 = System.nanoTime();

        GrayBlockCompression bc = new GrayBlockCompression();

        List<GrayRangeMatch> rangeMatches = bc.compress(metadata);

        long t1 = System.nanoTime();

        System.out.println("Starting Decompression");

        // declare decode images
        GrayPixel[][] img = new GrayPixel[metadata.getWidth()][metadata.getHeight()];
        GrayPixel[][] next = new GrayPixel[metadata.getWidth()][metadata.getHeight()];

        // fill random values
        MatrixUtils.fill(img, () -> GrayPixelUtils.getRandomPixel());
        MatrixUtils.fill(next, () -> GrayPixelUtils.getRandomPixel());

        PGMAUtils.saveToImage(img, inRunDir(runDir, "initial").toString());

        System.out.println("Iterating over " + iterations + " iterations");

        SimpleCompressionBenchmark scb = new SimpleCompressionBenchmark();

        // N iterations
        for (int iter = 0; iter <= iterations; iter++) {

            long iterationStartTs = System.nanoTime();

            System.out.println("Iteration " + iter);

            // loop compressed blocks
            for (GrayRangeMatch rm : rangeMatches) {
                GrayCompressedBlock cb = rm.compressed();

                int xDomain = cb.domain().x();
                int yDomain = cb.domain().y();

                GrayPixel[][] domainPixels = new GrayPixel[RGBBlockCompression.DBD][RGBBlockCompression.DBD];

                for (int i = 0; i < RGBBlockCompression.DBD; i++) {
                    for (int j = 0; j < RGBBlockCompression.DBD; j++) {
                        domainPixels[i][j] = img[xDomain + i][yDomain + j];
                    }
                }

                GrayBlock domainBlock = new GrayBlock(xDomain, yDomain, domainPixels);

                GrayBlock reducedBlock = domainBlock.reduce(RGBBlockCompression.RBD);

                GrayPixel[][] newPixels = new GrayPixel[RGBBlockCompression.RBD][RGBBlockCompression.RBD];

                for (int i = 0; i < RGBBlockCompression.RBD; i++) {
                    for (int j = 0; j < RGBBlockCompression.RBD; j++) {
                        GrayPixel p = reducedBlock.pixels()[i][j];

                        // gray has the same value for all colors
                        // here we take red to simplify it, for now.
                        float gray = p.gray();

                        // for gray its just one pixel for s and o.
                        float s = cb.s();
                        float o = cb.o();

                        // new generated value with s/o
                        int newColorValue = Math.max(0, Math.min(255, (int) (s * gray + o)));

                        GrayPixel newPixel = new GrayPixel(newColorValue);

                        newPixels[i][j] = newPixel;
                    }
                }

                // use range in rm to write the new pixels in next pixels

                int xRangeOffset = rm.range().x();
                int yRangeOffset = rm.range().y();

                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4; j++) {
                        next[xRangeOffset + i][yRangeOffset + j] = newPixels[i][j];
                    }
                }
            }

            long iterationEndTs = System.nanoTime();

            Path iterBase = inRunDir(runDir, "iter_" + iter);
            String currentIterationPath = iterBase + ".pgm";
            PGMAUtils.saveToImage(next, iterBase.toString());

            double mse = PGMAUtils.calculateMSE(originalImagePath, currentIterationPath);
            System.out.println("MSE: " + mse);

            double psnr = calculatePSNR(mse);
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

        scb.saveTo(runDir.toString());

    }

}
