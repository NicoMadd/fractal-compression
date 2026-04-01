package implementations.java.compression.src.utils.files.readers;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class SequenceReader {

    BufferedInputStream bis;

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
}
