package implementations.java.compression.src.utils.matrix;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class MatrixUtils {

    public static <T> List<T> flatMap(T[][] matrix) {
        return Arrays.stream(matrix)
                .flatMap(Arrays::stream)
                .toList();
    }

    public static <T> void fill(T[][] matrix, Supplier<T> supplier) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = supplier.get();
            }
        }
    }

    public static void fill(float[][] matrix, float value) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = value;
            }
        }
    }

    public static <T> void print(T[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(matrix[i][j].toString());
                System.out.print(' ');
            }
            System.out.println();
        }
    }

    public static void print(float[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(matrix[i][j]);
                System.out.print(' ');
            }
            System.out.println();
        }
    }

    public static float[][] avgKernel(int dim) {
        float[][] matrix = new float[dim][dim];

        float avgKernelValue = (float) 1 / (dim * 2);

        fill(matrix, avgKernelValue);

        return matrix;
    }

    /**
     * Dot product of two matrix.
     * 
     * The one restriction is that the matrix a should have
     * the same number of cols as rows of the matrix b.
     * 
     * @param a
     * @param b
     * @return
     */
    public static float dotProduct(float[][] a, float[][] b) {

        int rowsA = a.length;
        int colsA = a[0].length;
        int rowsB = b.length;

        if (colsA != rowsB) {
            throw new IllegalArgumentException("Matrix a should have the same number of columns as rows on Matrix b");
        }

        float sum = 0;

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsA; j++) {
                sum += a[i][j] * b[j][i];
            }
        }

        return sum;

    }

    /**
     * Copies a {@code dim}×{@code dim} square from {@code srcMatrix} into
     * {@code dstMatrix} at indices {@code [0..dim-1][0..dim-1]}.
     * <p>
     * Source coordinates {@code fromX + i} and {@code fromY + j} are clamped to the
     * source bounds (edge replication): indices below zero use row/column 0;
     * indices at
     * or past the edge use the last row/column. This avoids out-of-range access
     * when
     * the requested window extends outside the source.
     * <p>
     * If a clamped source cell is {@code null}, the corresponding destination cell
     * is
     * left unchanged.
     *
     * @param srcMatrix non-empty source matrix; assumed rectangular (all rows same
     *                  length)
     * @param dstMatrix destination matrix with at least {@code dim} rows and
     *                  {@code dim} columns per row
     * @param fromX     starting row in {@code srcMatrix} (first index)
     * @param fromY     starting column in {@code srcMatrix} (second index)
     * @param dim       side length of the square region to copy
     */
    public static <T> void copySquare(T[][] srcMatrix, T[][] dstMatrix, int fromX, int fromY, int dim) {

        int srcMatrixRows = srcMatrix.length;
        int srcMatrixCols = srcMatrix[0].length;

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {

                int finalX = Math.min(Math.max(fromX + i, 0), srcMatrixRows - 1);
                int finalY = Math.min(Math.max(fromY + j, 0), srcMatrixCols - 1);

                T value = srcMatrix[finalX][finalY];

                if (value != null) {
                    dstMatrix[i][j] = value;
                }
            }
        }
    }

    public static <T> float[][] floatMap(T[][] srcMatrix, Function<T, Float> mapper) {
        int rows = srcMatrix.length;
        int cols = srcMatrix[0].length;

        float[][] mappedMatrix = new float[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                mappedMatrix[i][j] = (float) mapper.apply(srcMatrix[i][j]);
            }
        }

        return mappedMatrix;
    }

}
