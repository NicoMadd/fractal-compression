package implementations.java.compression.src.utils.files.readers;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BytesReader implements SequenceInput {

    private final BufferedInputStream in;

    public BytesReader(String filepath) throws IOException {
        this.in = new BufferedInputStream(new FileInputStream(filepath));
    }

    @Override
    public char readChar() throws IOException {
        int b = in.read();
        if (b < 0) {
            throw new EOFException();
        }
        return (char) (b & 0xFF);
    }

    @Override
    public String readString(int utf8ByteCount) throws IOException {
        byte[] buf = in.readNBytes(utf8ByteCount);
        return new String(buf, StandardCharsets.UTF_8);
    }

    @Override
    public short readShort() throws IOException {
        int hi = in.read();
        int lo = in.read();
        if (hi < 0 || lo < 0) {
            throw new EOFException();
        }
        return (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
    }

    @Override
    public int readInt() throws IOException {
        byte[] b = in.readNBytes(4);
        return ((b[0] & 0xFF) << 24)
                | ((b[1] & 0xFF) << 16)
                | ((b[2] & 0xFF) << 8)
                | (b[3] & 0xFF);
    }

    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public void space() throws IOException {
        int c = in.read();
        if (c < 0) {
            throw new EOFException();
        }
        if (c != ' ') {
            throw new IOException("expected space (0x20), got 0x" + Integer.toHexString(c));
        }
    }

    @Override
    public void bl() throws IOException {
        int c = in.read();
        if (c < 0) {
            throw new EOFException();
        }
        if (c != '\n') {
            throw new IOException("expected newline (0x0a), got 0x" + Integer.toHexString(c));
        }
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
