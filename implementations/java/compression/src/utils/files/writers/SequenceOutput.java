package implementations.java.compression.src.utils.files.writers;

import java.io.IOException;

/**
 * Shared API for writing delimiter-oriented field sequences (text or binary
 * backends).
 */
public interface SequenceOutput extends AutoCloseable {

    void write(char c) throws IOException;

    void write(String text) throws IOException;

    void write(short number) throws IOException;

    void write(int number) throws IOException;

    void write(float number) throws IOException;

    void write(float number, int precision) throws IOException;

    void space() throws IOException;

    void bl() throws IOException;
}
