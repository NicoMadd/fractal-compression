package implementations.java.compression.src.utils.files.readers;

import java.io.IOException;

import implementations.java.compression.src.utils.files.writers.SequenceOutput;

/**
 * Shared API for reading delimiter-oriented field sequences (text or binary
 * backends).
 * Mirrors {@link SequenceOutput}.
 */
public interface SequenceInput extends AutoCloseable {

    /**
     * Reads one byte and returns it as a {@code char} (unsigned low 8 bits),
     * matching
     * {@link SequenceOutput#write(char)} / {@code BytesWriter}.
     */
    char readChar() throws IOException;

    /**
     * Reads exactly {@code utf8ByteCount} bytes and decodes as UTF-8, matching a
     * prior
     * {@link SequenceOutput#write(String)} of that length.
     */
    String readString(int utf8ByteCount) throws IOException;

    short readShort() throws IOException;

    int readInt() throws IOException;

    float readFloat() throws IOException;

    /** Consumes a single space, matching {@link SequenceOutput#space()}. */
    void space() throws IOException;

    /**
     * Consumes a line break ({@code '\n'}), matching {@link SequenceOutput#bl()}.
     */
    void bl() throws IOException;

    @Override
    void close() throws IOException;
}
