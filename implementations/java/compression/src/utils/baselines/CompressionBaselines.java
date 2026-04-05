package implementations.java.compression.src.utils.baselines;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import implementations.java.compression.src.utils.files.FileUtils;
import implementations.java.compression.src.utils.image.pgm.PGMAImageMetadata;
import implementations.java.compression.src.utils.image.pixel.GrayPixel;

/**
 * Benchmark baselines: write lossless PNG (from PGM) and DEFLATE zip archives on disk;
 * returned sizes match the written files.
 */
public final class CompressionBaselines {

    private CompressionBaselines() {
    }

    public static long writePngFromPgm(Path pgmPath, Path outputPngPath) throws IOException {
        if (outputPngPath.getParent() != null) {
            Files.createDirectories(outputPngPath.getParent());
        }
        try (FileInputStream in = new FileInputStream(pgmPath.toFile())) {
            PGMAImageMetadata meta = new PGMAImageMetadata(in);
            int w = meta.getWidth();
            int h = meta.getHeight();
            GrayPixel[][] px = meta.getPixels();
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    img.getRaster().setSample(x, y, 0, px[y][x].gray());
                }
            }
            try (FileOutputStream fos = new FileOutputStream(outputPngPath.toFile())) {
                if (!ImageIO.write(img, "png", fos)) {
                    throw new IOException("No PNG ImageIO writer available");
                }
            }
        }
        return FileUtils.getFileSize(outputPngPath);
    }

    public static long writeZipDeflatedArchive(Path sourcePath, Path outputZipPath, String entryName)
            throws IOException {
        if (outputZipPath.getParent() != null) {
            Files.createDirectories(outputZipPath.getParent());
        }
        byte[] raw = Files.readAllBytes(sourcePath);
        try (FileOutputStream fos = new FileOutputStream(outputZipPath.toFile());
                ZipOutputStream zos = new ZipOutputStream(fos)) {
            zos.setLevel(9);
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            zos.write(raw);
            zos.closeEntry();
        }
        return FileUtils.getFileSize(outputZipPath);
    }
}
