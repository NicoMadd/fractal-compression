package implementations.java.compression.src.utils.files.writers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BytesWriter implements SequenceOutput {

    private final FileOutputStream out;

    public BytesWriter(String filepath) throws IOException {
        this.out = new FileOutputStream(filepath);
    }

    public void write(char c) throws IOException {
        out.write(c);
    }

    public void write(String text) throws IOException {
        out.write(text.getBytes(StandardCharsets.UTF_8));
    }

    public void write(short number) throws IOException {
        out.write((number >>> 8) & 0xFF);
        out.write(number & 0xFF);
    }

    public void write(int number) throws IOException {
        out.write((number >>> 24) & 0xFF);
        out.write((number >>> 16) & 0xFF);
        out.write((number >>> 8) & 0xFF);
        out.write(number & 0xFF);
    }

    public void write(float number) throws IOException {
        int bits = Float.floatToIntBits(number);
        out.write((bits >>> 24) & 0xFF);
        out.write((bits >>> 16) & 0xFF);
        out.write((bits >>> 8) & 0xFF);
        out.write(bits & 0xFF);
    }

    /**
     * Same as {@link #write(float)}: four big-endian IEEE-754 bytes.
     * {@code precision} is ignored
     * (kept for {@link SequenceOutput} compatibility with text backends).
     */
    @Override
    public void write(float number, int precision) throws IOException {
        write(number);
    }

    public void space() throws IOException {
        write(' ');
    }

    public void bl() throws IOException {
        write('\n');
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

}
