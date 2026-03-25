package implementations.java.compression.src.utils.matrix;

import java.util.Arrays;
import java.util.List;
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

    public static <T> void print(T[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.println(matrix[i][j].toString());
            }
        }

    }

}
