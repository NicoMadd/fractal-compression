package implementations.java.compression.src.utils.image.pixel;

import java.io.FileOutputStream;
import java.io.IOException;

public abstract class Pixel {
    public abstract void addToFile(FileOutputStream fos) throws IOException;

    public abstract int red();

    public abstract int green();

    public abstract int blue();

    public int color(RGB rgb) {
        return switch (rgb) {
            case RED -> red();
            case GREEN -> green();
            case BLUE -> blue();
        };
    }

    public int color(int index) {
        RGB color = switch (index) {
            case 0 -> RGB.RED;
            case 1 -> RGB.GREEN;
            case 2 -> RGB.BLUE;
            default -> RGB.RED;
        };

        return color(color);

    }

    public double sqDiff(Pixel other) {
        int otherRed = other.red();
        int otherGreen = other.green();
        int otherBlue = other.blue();

        double diffRed = this.red() - otherRed;
        double diffGreen = this.green() - otherGreen;
        double diffBlue = this.blue() - otherBlue;

        return Math.pow(diffRed + diffGreen + diffBlue, 2);
    };
}
