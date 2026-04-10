package implementations.java.compression.src.pipelines;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import implementations.java.compression.src.benchmarks.MemorySampler;
import implementations.java.compression.src.utils.files.FileUtils;

public abstract class Pipeline {

    protected final MemorySampler memorySampler = new MemorySampler();

    public Pipeline(PipelineParams params) throws IOException {

        try (FileInputStream fis = new FileInputStream(params.imagePath())) {
            RunLogging.debug("File Input Stream opened.");
            init(fis);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        } finally {
            RunLogging.debug("File Input Stream closed.");
        }

        Files.createDirectories(FileUtils.PROCESSES_ROOT);
        Files.createDirectories(params.runDir());
        FileUtils.cleanAndCreateIterationsDir(params.runDir());
        FileUtils.copyOriginal(params.imagePath(), params.runDir());

    }

    /** First heap/runtime sample for a run; call near the start of {@code run} after run paths are known. */
    protected void beginRunMemorySampling() {
        memorySampler.sample();
    }

    /*
     * Initialize the image metadata and sources needed for compression pipeline.
     * 
     */
    protected abstract void init(FileInputStream fis) throws IOException;
}
