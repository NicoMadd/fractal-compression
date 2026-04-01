package implementations.java.compression.src.pipelines;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import implementations.java.compression.src.utils.files.FileUtils;

public abstract class Pipeline {

    public Pipeline(PipelineParams params) throws IOException {

        try (FileInputStream fis = new FileInputStream(params.imagePath())) {
            System.out.println("File Input Stream opened.");
            init(fis);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } finally {
            System.out.println("File Input Stream closed.");
        }

        Files.createDirectories(FileUtils.PROCESSES_ROOT);
        Files.createDirectories(params.runDir());
        FileUtils.cleanAndCreateIterationsDir(params.runDir());
        FileUtils.copyOriginal(params.imagePath(), params.runDir());

    }

    /*
     * Initialize the image metadata and sources needed for compression pipeline.
     * 
     */
    protected abstract void init(FileInputStream fis) throws IOException;
}
