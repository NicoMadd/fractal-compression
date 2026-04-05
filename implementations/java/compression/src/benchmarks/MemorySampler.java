package implementations.java.compression.src.benchmarks;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

public final class MemorySampler {

    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private long peakHeapUsed = 0;
    private long peakRuntimeUsed = 0;
    private long sumHeapUsed = 0;
    private long sumRuntimeUsed = 0;
    private int sampleCount = 0;

    public void sample() {
        MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
        long heapUsed = heap.getUsed();
        if (heapUsed > peakHeapUsed) {
            peakHeapUsed = heapUsed;
        }
        Runtime rt = Runtime.getRuntime();
        long rtUsed = rt.totalMemory() - rt.freeMemory();
        if (rtUsed > peakRuntimeUsed) {
            peakRuntimeUsed = rtUsed;
        }
        sumHeapUsed += heapUsed;
        sumRuntimeUsed += rtUsed;
        sampleCount++;
    }

    public long peakHeapUsedBytes() {
        return peakHeapUsed;
    }

    public long peakRuntimeUsedBytes() {
        return peakRuntimeUsed;
    }

    /** Arithmetic mean heap used over all {@link #sample()} calls (0 if none). */
    public long avgHeapUsedBytes() {
        return sampleCount == 0 ? 0 : sumHeapUsed / sampleCount;
    }

    /** Arithmetic mean of {@code totalMemory - freeMemory} over all samples (0 if none). */
    public long avgRuntimeUsedBytes() {
        return sampleCount == 0 ? 0 : sumRuntimeUsed / sampleCount;
    }
}
