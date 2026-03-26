package implementations.java.compression.src.utils.image.pixel;

public enum MaxValType {
    SINGLE(1), DOUBLE(2);

    private int value;

    private MaxValType(int n) {
        this.value = n;
    }

    public int size() {
        return this.value;
    }
}
