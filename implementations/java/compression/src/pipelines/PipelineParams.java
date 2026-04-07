package implementations.java.compression.src.pipelines;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import implementations.java.compression.src.utils.files.FileUtils;
import implementations.java.compression.src.utils.fractal.block.reductions.BicubicReductionStrategy;
import implementations.java.compression.src.utils.fractal.block.reductions.MeanReductionStrategy;
import implementations.java.compression.src.utils.fractal.block.reductions.ReductionStrategy;

public record PipelineParams(String imagePath, int iterations, Path runDir, int rangeSize, int domainSize,
        boolean cleanCodebook, ReductionStrategy reductionStrategy) {

    public static Optional<PipelineParams> parse(String[] args) {
        Integer rangeFlag = null;
        Integer domainFlag = null;
        boolean cleanCodebook = false;
        boolean sawMr = false;
        boolean sawBr = false;

        List<String> positionals = new ArrayList<>();
        for (int i = 0; i < args.length;) {
            String a = args[i];
            if ("-r".equals(a)) {
                if (i + 1 >= args.length) {
                    System.out.println("Usage: -r requires a range size (positive integer).");
                    return Optional.empty();
                }
                try {
                    rangeFlag = Integer.parseInt(args[i + 1]);
                    if (rangeFlag <= 0) {
                        System.out.println("Range size must be a positive integer.");
                        return Optional.empty();
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Range size must be an integer.");
                    return Optional.empty();
                }
                i += 2;
            } else if ("-d".equals(a)) {
                if (i + 1 >= args.length) {
                    System.out.println("Usage: -d requires a domain size (positive integer).");
                    return Optional.empty();
                }
                try {
                    domainFlag = Integer.parseInt(args[i + 1]);
                    if (domainFlag <= 0) {
                        System.out.println("Domain size must be a positive integer.");
                        return Optional.empty();
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Domain size must be an integer.");
                    return Optional.empty();
                }
                i += 2;
            } else if ("-c".equals(a)) {
                cleanCodebook = true;
                i += 1;
            } else if ("--mr".equals(a)) {
                sawMr = true;
                i += 1;
            } else if ("--br".equals(a)) {
                sawBr = true;
                i += 1;
            } else {
                positionals.add(a);
                i += 1;
            }
        }

        if (sawMr && sawBr) {
            System.out.println("Cannot specify both --mr and --br.");
            return Optional.empty();
        }

        ReductionStrategy reductionStrategy = sawBr ? new BicubicReductionStrategy() : new MeanReductionStrategy();

        if (positionals.size() < 2) {
            System.out.println(
                    "Usage: <image path> <iterations> [<range size> [<domain size>]] [-r <range>] [-d <domain>] [-c] [--mr | --br]");
            System.out.println(
                    "  Default range size is 4 if omitted. Domain defaults to 2× range if omitted. Domain reduction defaults to mean; --mr is mean, --br is bicubic.");
            return Optional.empty();
        }

        String imagePath = positionals.get(0);
        int iterations;
        try {
            iterations = Integer.parseInt(positionals.get(1));
            if (iterations < 0) {
                System.out.println("Iterations must be non-negative.");
                return Optional.empty();
            }
        } catch (NumberFormatException e) {
            System.out.println("Iterations must be an integer.");
            return Optional.empty();
        }

        int rangeSize;
        if (rangeFlag != null) {
            rangeSize = rangeFlag;
        } else if (positionals.size() >= 3) {
            try {
                rangeSize = Integer.parseInt(positionals.get(2));
                if (rangeSize <= 0) {
                    System.out.println("Range size must be a positive integer.");
                    return Optional.empty();
                }
            } catch (NumberFormatException e) {
                System.out.println("Range size must be an integer.");
                return Optional.empty();
            }
        } else {
            rangeSize = 4;
        }

        int domainSize;
        if (domainFlag != null) {
            domainSize = domainFlag;
        } else if (positionals.size() >= 4) {
            try {
                domainSize = Integer.parseInt(positionals.get(3));
                if (domainSize <= 0) {
                    System.out.println("Domain size must be a positive integer.");
                    return Optional.empty();
                }
            } catch (NumberFormatException e) {
                System.out.println("Domain size must be an integer.");
                return Optional.empty();
            }
        } else {
            domainSize = rangeSize * 2;
        }

        Path runDir = FileUtils.PROCESSES_ROOT.resolve(FileUtils.runFolderName(imagePath));

        return Optional
                .of(new PipelineParams(imagePath, iterations, runDir, rangeSize, domainSize, cleanCodebook, reductionStrategy));
    }

}
