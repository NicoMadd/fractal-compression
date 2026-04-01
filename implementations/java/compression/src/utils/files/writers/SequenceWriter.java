package implementations.java.compression.src.utils.files.writers;

import java.io.FileWriter;
import java.io.IOException;

public class SequenceWriter implements AutoCloseable {

    private FileWriter fw;

    public SequenceWriter(String filepath) throws IOException {
        this.fw = new FileWriter(filepath);
    }

    public void write(char c) throws IOException {
        this.fw.write(c);
    }

    public void write(String text) throws IOException {
        char[] chars = text.toCharArray();
        this.fw.write(chars);
    }

    public void write(int number) throws IOException {
        write(String.valueOf(number));
    }

    public void write(float number) throws IOException {
        write(String.valueOf(number));
    }

    public void space() throws IOException {
        write(' ');
    }

    public void bl() throws IOException {
        write('\n');
    }

    @Override
    public void close() throws Exception {
        this.fw.close();
    }
}
