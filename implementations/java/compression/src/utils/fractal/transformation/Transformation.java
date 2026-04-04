package implementations.java.compression.src.utils.fractal.transformation;

public abstract class Transformation {

    protected TransformationType type;

    public Transformation(TransformationType type) {
        this.type = type;
    }

    public TransformationType type() {
        return this.type;
    }

    public abstract <T> void transform(T[][] src, T[][] transformed);

}
