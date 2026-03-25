package implementations.java.compression.src.benchmarks;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SimpleCompressionBenchmark {

    List<Iteration> iterations;

    public SimpleCompressionBenchmark() {
        this.iterations = new ArrayList<>();
    }

    public void add(Iteration iteration) {
        this.iterations.add(iteration);
    }

    public void saveTo(String directory) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(Iteration.columns());
        sb.append('\n');
        for (Iteration it : iterations) {
            sb.append(it.toString());
            sb.append('\n');
        }
        Path file = Path.of(directory, "benchmark.csv");
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }

}
