package implementations.java.compression.src.utils.fractal.transformation;

import java.util.Objects;

import implementations.java.compression.src.utils.fractal.transformation.definitions.Identity;
import implementations.java.compression.src.utils.fractal.transformation.definitions.ReflectionFd;
import implementations.java.compression.src.utils.fractal.transformation.definitions.ReflectionHz;
import implementations.java.compression.src.utils.fractal.transformation.definitions.ReflectionSd;
import implementations.java.compression.src.utils.fractal.transformation.definitions.ReflectionVc;
import implementations.java.compression.src.utils.fractal.transformation.definitions.RotationCCW90;
import implementations.java.compression.src.utils.fractal.transformation.definitions.RotationCW180;
import implementations.java.compression.src.utils.fractal.transformation.definitions.RotationCW90;

/**
 * Builds {@link Transformation} instances for a {@link TransformationType}.
 * Each call returns a new instance (stateless, safe to reuse or discard).
 */
public final class TransformationFactory {

    private TransformationFactory() {
    }

    public static Transformation of(TransformationType type) {
        Objects.requireNonNull(type, "type");
        return switch (type) {
            case IDENTITY -> new Identity();
            case ROTATION_CW_90 -> new RotationCW90();
            case ROTATION_CW_180 -> new RotationCW180();
            case ROTATION_CCW_90 -> new RotationCCW90();
            case REFLECTION_HZ -> new ReflectionHz();
            case REFLECTION_VC -> new ReflectionVc();
            case REFLECTION_FD -> new ReflectionFd();
            case REFLECTION_SD -> new ReflectionSd();
            default -> throw new IllegalArgumentException("Invalid transformation type: " + type);
        };
    }
}
