package implementations.java.compression.src.utils.fractal.mapping;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import implementations.java.compression.src.utils.files.readers.SequenceReader;
import implementations.java.compression.src.utils.files.writers.SequenceWriter;

public class Codebook {

    private List<FractalMapping> mappings;

    private String MAGIC_NUMBER = "FC";

    public Codebook(List<FractalMapping> mappings) {
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
        try (FileInputStream fis = new FileInputStream(path)) {
            SequenceReader sr = new SequenceReader(fis);
            deserialize(sr);
        } catch (Exception e) {
            System.out.println("Error deserializing codebook: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<FractalMapping> getMappings() {
        return this.mappings;
    }

    private void deserialize(SequenceReader sr) throws IOException {

        List<FractalMapping> mappings = new ArrayList<>();

        // Read first two bytes. Should be magic number
        String magicNumber = sr.readNBytesAsString(2);

        if (!MAGIC_NUMBER.equals(magicNumber)) {
            throw new IllegalArgumentException(
                    "Magic number of image is not correct. Expected " + MAGIC_NUMBER + "but was: " + magicNumber);
        }

        // Read a space
        sr.readWhitespace();

        // Read an int with a max rows to read for all fractal mappings
        int totalRows = sr.readNextInt();

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
    private FractalMapping readMapping(SequenceReader sr) throws IOException {
        int rangeX = sr.readNextInt();
        int rangeY = sr.readNextInt();
        int domainX = sr.readNextInt();
        int domainY = sr.readNextInt();
        float s = sr.readNextFloat();
        float o = sr.readNextFloat();

        return new FractalMapping(rangeX, rangeY, domainX, domainY, s, o);
    }

    /**
     * Serializes the Codebook into a file.
     * 
     * @param path where to save the serialization
     * @throws Exception
     */
    public void save(String path) throws Exception {

        try (SequenceWriter sw = new SequenceWriter(path)) {
            serialize(sw);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void serialize(SequenceWriter sw) throws IOException {

        // Write the MAGIC_NUMBER
        sw.write(MAGIC_NUMBER);

        // Write a space
        sw.space();

        // total rows
        sw.write(this.mappings.size());

        // Breakline
        sw.bl();

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
            sw.write(fm.s());
            sw.space();
            sw.write(fm.o());
            sw.bl();
        }

    }

}
