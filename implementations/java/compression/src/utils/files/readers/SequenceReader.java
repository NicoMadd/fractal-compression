package implementations.java.compression.src.utils.files.readers;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SequenceReader implements SequenceInput {

    BufferedInputStream bis;

    public SequenceReader(String path) throws FileNotFoundException {
        this(new FileInputStream(path));
    }

    public SequenceReader(FileInputStream bis) {
        this.bis = new BufferedInputStream(bis);
    }

    public void skipFollowingWhitespaces() throws IOException {

        char c;

        do {

            this.bis.mark(1);
            int ic = this.bis.read();
            c = (char) ic;

        } while (Character.isWhitespace(c));

        this.bis.reset();

    }

    public byte[] readUntilWhitespace() throws IOException {
        int arraySize = 10;
        byte[] bytes = new byte[arraySize];
        int idx = 0;

        do {
            this.bis.mark(1);
            int ic = this.bis.read();
            char c = (char) ic;

            if (Character.isWhitespace(c)) {
                this.bis.reset();
                return Arrays.copyOfRange(bytes, 0, idx);
            }

            bytes[idx++] = (byte) c;

            // topped array -> should increase
            if (idx >= arraySize) {
                arraySize += 10;
                bytes = Arrays.copyOf(bytes, arraySize);
            }

        } while (true);
    }

    public char readWhitespace() throws IOException {
        int ic = this.bis.read();

        if (ic == -1) {
            throw new EOFException();
        }

        char c = (char) ic;

        if (!Character.isWhitespace(c)) {
            throw new RuntimeException("Character is not a whitespace. its: " + c);
        }

        return c;
    }

    public byte[] readNBytes(int n) throws IOException {
        return this.bis.readNBytes(n);
    }

    public byte read() throws IOException {
        return (byte) this.bis.read();
    }

    public boolean nextCharIs(char value) throws IOException {
        this.bis.mark(1);
        char readValue = (char) this.bis.read();
        // System.out.println("reading next char: " + readValue + " " + (int)
        // readValue);
        boolean isEqual = readValue == value;
        this.bis.reset();
        return isEqual;
    }

    public void skipUntil(char untilValue) throws IOException {
        while (untilValue != (char) this.bis.read()) {
        }
    }

    public void skipUntilLineBreak() throws IOException {
        while (!this.nextCharIs('\n')) {
            this.read();
        }
        this.readWhitespace();

    }

    public int readNBytesAsInt(int n) throws IOException {
        byte[] bytes = this.readNBytes(n);
        return Integer.parseInt(new String(bytes));
    }

    public String readNBytesAsString(int n) throws IOException {
        byte[] bytes = this.readNBytes(n);
        return new String(bytes);
    }

    public int readNextInt() throws IOException {
        skipFollowingWhitespaces();
        byte[] readBytes = readUntilWhitespace();
        String rawNumber = new String(readBytes);
        return Integer.parseInt(rawNumber);
    }

    public float readNextFloat() throws IOException {
        skipFollowingWhitespaces();
        byte[] readBytes = readUntilWhitespace();
        String rawNumber = new String(readBytes);
        return Float.parseFloat(rawNumber);
    }

    @Override
    public char readChar() throws IOException {
        int b = bis.read();
        if (b < 0) {
            throw new EOFException();
        }
        return (char) (b & 0xFF);
    }

    @Override
    public String readString(int utf8ByteCount) throws IOException {
        byte[] buf = bis.readNBytes(utf8ByteCount);
        return new String(buf, StandardCharsets.UTF_8);
    }

    @Override
    public short readShort() throws IOException {
        skipFollowingWhitespaces();
        byte[] readBytes = readUntilWhitespace();
        return Short.parseShort(new String(readBytes, StandardCharsets.UTF_8));
    }

    @Override
    public int readInt() throws IOException {
        skipFollowingWhitespaces();
        byte[] readBytes = readUntilWhitespace();
        return Integer.parseInt(new String(readBytes, StandardCharsets.UTF_8));
    }

    @Override
    public float readFloat() throws IOException {
        return readNextFloat();
    }

    @Override
    public void space() throws IOException {
        int c = bis.read();
        if (c < 0) {
            throw new EOFException();
        }
        if (c != ' ') {
            throw new IOException("expected space (0x20), got 0x" + Integer.toHexString(c));
        }
    }

    @Override
    public void bl() throws IOException {
        int c = bis.read();
        if (c < 0) {
            throw new EOFException();
        }
        if (c != '\n') {
            throw new IOException("expected newline (0x0a), got 0x" + Integer.toHexString(c));
        }
    }

    @Override
    public void close() throws IOException {
        bis.close();
    }
}
