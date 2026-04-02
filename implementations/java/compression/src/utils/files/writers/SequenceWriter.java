package implementations.java.compression.src.utils.files.writers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class SequenceWriter implements SequenceOutput {

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

    public void write(short number) throws IOException {
        write(String.valueOf(number));
    }

    public void write(int number) throws IOException {
        write(String.valueOf(number));
    }

    public void write(float number) throws IOException {
        write(number, 2);
    }

    public void write(float number, int precision) throws IOException {
        String formatted = String.format(Locale.US, "%." + precision + "f", number);
        write(formatted);
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
