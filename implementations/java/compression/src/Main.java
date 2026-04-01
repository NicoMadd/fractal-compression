package implementations.java.compression.src;

import java.util.Optional;

import implementations.java.compression.src.pipelines.PGMAPipeline;
import implementations.java.compression.src.pipelines.PipelineParams;

public class Main {

    public static void main(String[] args) throws Exception {

        Optional<PipelineParams> parsed = PipelineParams.parse(args);
        if (parsed.isEmpty()) {
            return;
        }
        PipelineParams params = parsed.get();

        PGMAPipeline pipeline = new PGMAPipeline(params);
        pipeline.run(params);

    }

}
