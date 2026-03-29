package implementations.java.compression.src.utils.fractal.block;

import implementations.java.compression.src.utils.image.pixel.Pixel;

/* 
x: is the x point of the left-top pixel of the pixels.
y: is the y point of the left-top pixel of the pixels.
*/
public abstract class Block<T extends Pixel> {
    int x;
    int y;
    protected T[][] pixels;

    public Block(int x, int y, T[][] pixels) {
        this.x = x;
        this.y = y;
        this.pixels = pixels;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public T[][] pixels() {
        return this.pixels;
    }

    /**
     * @param reduceTo dimension to reduce the block of pixels to
     * @return a new Block with the array reduced to a square matrix of reduceTo
     *         dim.
     */
    public abstract Block<T> reduce(int reduceTo);

    public String toString() {
        int rows = this.pixels().length;

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < this.pixels()[i].length; j++) {
                sb.append(this.pixels()[i][j]);
                sb.append(' ');
            }
            sb.append('\n');
        }

        return sb.toString();

    }

}
