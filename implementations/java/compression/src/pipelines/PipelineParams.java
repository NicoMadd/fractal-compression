package implementations.java.compression.src.pipelines;

import java.nio.file.Path;
import java.util.Optional;

import implementations.java.compression.src.utils.files.FileUtils;

public record PipelineParams(String imagePath, int iterations, Path runDir, int rangeSize, int domainSize) {

    public static Optional<PipelineParams> parse(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: <image path> <iterations> <range size>");
            return Optional.empty();
        }

        String imagePath = args[0];
        int iterations;
        try {
            iterations = Integer.parseInt(args[1]);
            if (iterations < 0) {
                System.out.println("Iterations must be non-negative.");
                return Optional.empty();
            }
        } catch (NumberFormatException e) {
            System.out.println("Iterations must be an integer.");
            return Optional.empty();
        }

        int rangeSize;
        try {
            rangeSize = Integer.parseInt(args[2]);
            if (rangeSize <= 0) {
                System.out.println("Range size must be a positive integer.");
                return Optional.empty();
            }
        } catch (NumberFormatException e) {
            System.out.println("Range size must be an integer.");
            return Optional.empty();
        }

        Path runDir = FileUtils.PROCESSES_ROOT.resolve(FileUtils.runFolderName(imagePath));

        int domainSize = rangeSize * 2;

        return Optional.of(new PipelineParams(imagePath, iterations, runDir, rangeSize, domainSize));
    }

}
