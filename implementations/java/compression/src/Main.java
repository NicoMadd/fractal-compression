package implementations.java.compression.src;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import implementations.java.compression.src.algorithms.BlockCompression;
import implementations.java.compression.src.benchmarks.Iteration;
import implementations.java.compression.src.benchmarks.SimpleCompressionBenchmark;
import implementations.java.compression.src.utils.fractal.Block;
import implementations.java.compression.src.utils.fractal.CompressedBlock;
import implementations.java.compression.src.utils.fractal.RangeMatch;
import implementations.java.compression.src.utils.image.pixel.IntPixel;
import implementations.java.compression.src.utils.image.pixel.Pixel;
import implementations.java.compression.src.utils.image.pixel.PixelUtils;
import implementations.java.compression.src.utils.image.pixel.RGB;
import implementations.java.compression.src.utils.image.ppm.PPMImageMetadata;
import implementations.java.compression.src.utils.image.ppm.PPMUtils;
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

        PPMImageMetadata metadata = null;

        // Load the image
        try (FileInputStream fis = new FileInputStream(imagePath)) {
            System.out.println("File Input Stream opened.");
            metadata = new PPMImageMetadata(fis);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } finally {
            System.out.println("File Input Stream closed.");
        }

        long t0 = System.nanoTime();

        BlockCompression bc = new BlockCompression();
        List<RangeMatch> rangeMatches = bc.compress(metadata);

        long t1 = System.nanoTime();

        System.out.println("Starting Decompression");

        // declare decode images
        Pixel[][] img = new Pixel[metadata.getWidth()][metadata.getHeight()];
        Pixel[][] next = new Pixel[metadata.getWidth()][metadata.getHeight()];

        // fill random values
        MatrixUtils.fill(img, () -> PixelUtils.getRandomPixel());
        MatrixUtils.fill(next, () -> PixelUtils.getRandomPixel());

        // Save random image
        PPMUtils.saveToImage(img, getStorageFile("initial.ppm"));

        int iterations = 25;

        System.out.println("Iterating over " + iterations + " iterations");

        SimpleCompressionBenchmark scb = new SimpleCompressionBenchmark();

        // N iterations
        for (int iter = 0; iter <= iterations; iter++) {

            long iterationStartTs = System.nanoTime();

            System.out.println("Iteration " + iter);

            // loop compressed blocks
            for (RangeMatch rm : rangeMatches) {
                CompressedBlock cb = rm.compressed();

                int xDomain = cb.domain().x();
                int yDomain = cb.domain().y();

                Pixel[][] domainPixels = new Pixel[BlockCompression.DBD][BlockCompression.DBD];

                for (int i = 0; i < BlockCompression.DBD; i++) {
                    for (int j = 0; j < BlockCompression.DBD; j++) {
                        domainPixels[i][j] = img[xDomain + i][yDomain + j];
                    }
                }

                Block domainBlock = new Block(xDomain, yDomain, domainPixels);

                Block reducedBlock = domainBlock.reduce(BlockCompression.RBD);

                Pixel[][] newPixels = new Pixel[BlockCompression.RBD][BlockCompression.RBD];

                for (int i = 0; i < BlockCompression.RBD; i++) {
                    for (int j = 0; j < BlockCompression.RBD; j++) {
                        Pixel p = reducedBlock.pixels()[i][j];

                        int[] newColors = new int[3];

                        for (RGB c : RGB.values()) {

                            float s = cb.s()[c.ordinal()];
                            float o = cb.o()[c.ordinal()];

                            // previous value
                            int colorValue = p.color(c);

                            // new generated value with s/o
                            int newColorValue = Math.max(0, Math.min(255, (int) (s * colorValue + o)));

                            newColors[c.ordinal()] = newColorValue;
                        }

                        Pixel newPixel = new IntPixel(
                                newColors[RGB.RED.ordinal()],
                                newColors[RGB.GREEN.ordinal()],
                                newColors[RGB.BLUE.ordinal()]);

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

            String currentIterationPath = getStorageFile("iter_" + iter + ".ppm");
            PPMUtils.saveToImage(next, currentIterationPath);

            double mse = PPMUtils.calculateMSE(imagePath, currentIterationPath);
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
