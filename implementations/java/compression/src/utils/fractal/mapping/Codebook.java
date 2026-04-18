package implementations.java.compression.src.utils.fractal.mapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import implementations.java.compression.src.utils.files.readers.SequenceInput;
import implementations.java.compression.src.utils.files.readers.SequenceReader;
import implementations.java.compression.src.utils.files.writers.SequenceOutput;
import implementations.java.compression.src.utils.files.writers.SequenceWriter;
import implementations.java.compression.src.utils.fractal.transformation.TransformationType;

public class Codebook {

    /** Subfolder under each stem’s {@code processes/java/<stem>/} where {@code codebook_r{r}_d{d}.fc} files live. */
    public static final String CODEBOOKS_SUBDIR = "codebooks";

    private int rangeSize;
    private int domainSize;
    private List<FractalMapping> mappings;

    private String MAGIC_NUMBER = "FC";

    public Codebook(int rangeSize, int domainSize, List<FractalMapping> mappings) {
        this.rangeSize = rangeSize;
        this.domainSize = domainSize;
        this.mappings = mappings;
    }

    /**
     * This constructor initializes the Codebook based on a path
     * on where to look for the codebook serialized file.
     * 
     * @param path Path to codebook file
     */

    public Codebook(String path) {
        this.mappings = List.of();
        try (SequenceInput sr = new SequenceReader(path)) {
            deserialize(sr);
        } catch (Exception e) {
            System.out.println("Error deserializing codebook: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<FractalMapping> getMappings() {
        return this.mappings;
    }

    /** Range block edge length stored in the file header. */
    public int rangeSize() {
        return rangeSize;
    }

    /** Domain block edge length stored in the file header. */
    public int domainSize() {
        return domainSize;
    }

    /**
     * Canonical on-disk path for this range/domain geometry under a stem’s {@code processes/java/<stem>/} folder
     * ({@link #CODEBOOKS_SUBDIR} subdirectory).
     */
    public static Path pathForGeometry(Path runDir, int rangeSize, int domainSize) {
        return runDir.resolve(CODEBOOKS_SUBDIR).resolve(String.format("codebook_r%d_d%d.fc", rangeSize, domainSize));
    }

    private void deserialize(SequenceInput sr) throws IOException {

        // Read first two bytes. Should be magic number
        String magicNumber = sr.readString(2);

        if (!MAGIC_NUMBER.equals(magicNumber)) {
            throw new IllegalArgumentException(
                    "Magic number of image is not correct. Expected " + MAGIC_NUMBER + "but was: " + magicNumber);
        }

        sr.space();
        this.rangeSize = sr.readInt();
        this.domainSize = sr.readInt();
        int totalRows = sr.readInt();

        List<FractalMapping> mappings = new ArrayList<>();

        for (int i = 0; i < totalRows; i++) {
            FractalMapping fm = readMapping(sr);
            mappings.add(fm);
        }

        this.mappings = mappings;
    }

    /**
     * The fractal mapping is composed of 4 ints and 2 floats.
     * 
     * @param sr
     * @return
     * @throws IOException
     */
    private FractalMapping readMapping(SequenceInput sr) throws IOException {
        int rangeX = sr.readInt();
        int rangeY = sr.readInt();
        int domainX = sr.readInt();
        int domainY = sr.readInt();
        float s = sr.readFloat();
        float o = sr.readFloat();

        TransformationType t = parseTransformation(sr);

        return new FractalMapping(rangeX, rangeY, domainX, domainY, s, o, t);
    }

    private TransformationType parseTransformation(SequenceInput sr) throws IOException {
        int ordinal = sr.readInt();
        return TransformationType.values()[ordinal];
    }

    /**
     * Serializes the Codebook into a file.
     * 
     * @param path where to save the serialization
     * @throws Exception
     */
    public void save(String path) throws Exception {
        Path p = Path.of(path);
        Path parent = p.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (SequenceOutput sw = new SequenceWriter(path)) {
            serialize(sw);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void serialize(SequenceOutput sw) throws IOException {

        serializeMetadata(sw);

        for (int i = 0; i < this.mappings.size(); i++) {
            FractalMapping fm = mappings.get(i);

            sw.write(fm.rangeX());
            sw.space();
            sw.write(fm.rangeY());
            sw.space();
            sw.write(fm.domainX());
            sw.space();
            sw.write(fm.domainY());
            sw.space();
            sw.write(fm.s(), 2);
            sw.space();
            sw.write(fm.o(), 2);
            sw.space();
            sw.write(fm.t().ordinal());

            sw.bl();
        }

    }

    private void serializeMetadata(SequenceOutput sw) throws IOException {
        // Write the MAGIC_NUMBER
        sw.write(MAGIC_NUMBER);

        // Write a space
        sw.space();

        sw.write(rangeSize);

        sw.space();

        sw.write(domainSize);

        sw.space();

        // total rows
        sw.write(this.mappings.size());

        // Breakline
        sw.bl();
    }

}
