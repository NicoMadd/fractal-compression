package implementations.java.compression.src;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
import implementations.java.compression.src.utils.image.pixel.GrayPixelUtils;
import implementations.java.compression.src.utils.matrix.MatrixUtils;

public class Main {

    private static String PWD = Paths.get("").toAbsolutePath().toString();
    private static String STORAGE_DIR = PWD.concat("/iterations/");

    private static double calculatePSNR(double mse) {
        if (mse == 0)
            return Double.POSITIVE_INFINITY;
        return 10 * Math.log10(Math.pow(255, 2) / mse);
    }

    private static String getStorageFile(String filename) {
        return STORAGE_DIR.concat(filename);
    }

    private static void createStorageFolder() throws IOException {
        String strPath = getStorageFile("");

        Path folderPath = Paths.get(strPath);

        if (!Files.isDirectory(folderPath)) {
            Files.createDirectories(folderPath);
        }
    }

    public static void main(String[] args) throws IOException {

        // Receive first argument as the path to the image

        if (args.length == 0) {
            System.out.println("Please provide the path to the image as the first argument.");
            return;
        }

        createStorageFolder();

        String imagePath = args[0];

        PGMAImageMetadata metadata = null;

        // Load the image
        try (FileInputStream fis = new FileInputStream(imagePath)) {
            System.out.println("File Input Stream opened.");
            metadata = new PGMAImageMetadata(fis);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } finally {
            System.out.println("File Input Stream closed.");
        }

        compressImage(imagePath, metadata);

    }

    private static void compressImage(String originalImagePath, PGMAImageMetadata metadata) throws IOException {

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

        // Save random image
        PGMAUtils.saveToImage(img, getStorageFile("initial"));

        int iterations = 10;

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

            String currentIterationPathRaw = getStorageFile("iter_" + iter);
            String currentIterationPath = currentIterationPathRaw + ".pgm";
            PGMAUtils.saveToImage(next, currentIterationPathRaw);

            double mse = PGMAUtils.calculateMSE(originalImagePath, currentIterationPath);
            System.out.println("MSE: " + mse);

            double psnr = calculatePSNR(mse);
            System.out.println("PSNR: " + psnr);
            Iteration iteration = new Iteration(iter, iterationEndTs, iterationEndTs - iterationStartTs, mse, psnr);
            scb.add(iteration);

            img = next;

        }

        long t2 = System.nanoTime();

        long compressionElapsedNanos = t1 - t0;
        double compressionSeconds = compressionElapsedNanos / 1_000_000_000.0;

        long decompressionElapsedNanos = t2 - t1;
        double decompressionSeconds = decompressionElapsedNanos / 1_000_000_000.0;

        System.out.println("Compression took: " + compressionSeconds + "s");
        System.out.println("Decompression took: " + decompressionSeconds + "s");

        scb.saveTo(getStorageFile(""));

    }

}
