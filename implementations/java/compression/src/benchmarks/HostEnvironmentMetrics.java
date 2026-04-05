package implementations.java.compression.src.benchmarks;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.util.Locale;
import java.util.StringJoiner;

import com.sun.management.OperatingSystemMXBean;

/**
 * Snapshot of logical CPUs, OS-reported memory, CPU load, and JVM configuration / heap at capture time.
 */
public record HostEnvironmentMetrics(
        int cpuLogicalCores,
        String osName,
        String osVersion,
        String osArch,
        long osTotalMemoryBytes,
        long osFreeMemoryBytes,
        Double processCpuLoad,
        Double systemCpuLoad,
        long jvmHeapInitBytes,
        long jvmHeapUsedBytes,
        long jvmHeapCommittedBytes,
        long jvmHeapMaxBytes,
        long jvmNonHeapCommittedBytes,
        long jvmRuntimeMaxMemoryBytes,
        String jvmVersion,
        String jvmRuntimeVersion,
        String jvmVmName,
        String jvmVmVendor,
        String jvmSpecVersion,
        String jvmInputArgsJoined) {

    @SuppressWarnings("deprecation")
    public static HostEnvironmentMetrics capture() {
        int cores = Runtime.getRuntime().availableProcessors();
        String osName = System.getProperty("os.name", "");
        String osVersion = System.getProperty("os.version", "");
        String osArch = System.getProperty("os.arch", "");
        String jvmVersion = System.getProperty("java.version", "");
        String jvmRuntimeVersion = System.getProperty("java.runtime.version", "");
        String jvmVmName = System.getProperty("java.vm.name", "");
        String jvmVmVendor = System.getProperty("java.vm.vendor", "");
        String jvmSpecVersion = System.getProperty("java.specification.version", "");

        long rtMax = Runtime.getRuntime().maxMemory();

        MemoryMXBean memMx = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memMx.getHeapMemoryUsage();
        MemoryUsage nh = memMx.getNonHeapMemoryUsage();
        long heapInit = heap.getInit();
        long heapUsed = heap.getUsed();
        long heapCommitted = heap.getCommitted();
        long heapMax = heap.getMax();

        RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
        StringJoiner sj = new StringJoiner(" ");
        for (String a : rt.getInputArguments()) {
            sj.add(a);
        }
        String jvmArgs = sj.toString();

        long totalPhys = -1L;
        long freePhys = -1L;
        Double procLoad = null;
        Double sysLoad = null;

        try {
            OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            totalPhys = osBean.getTotalPhysicalMemorySize();
            freePhys = osBean.getFreePhysicalMemorySize();
            double p = osBean.getProcessCpuLoad();
            if (p >= 0d) {
                procLoad = p;
            }
            double s = osBean.getSystemCpuLoad();
            if (s >= 0d) {
                sysLoad = s;
            }
        } catch (ClassCastException | UnsupportedOperationException ignored) {
            // Non-HotSpot or restricted VM
        }

        return new HostEnvironmentMetrics(
                cores,
                osName,
                osVersion,
                osArch,
                totalPhys,
                freePhys,
                procLoad,
                sysLoad,
                heapInit,
                heapUsed,
                heapCommitted,
                heapMax,
                nh.getCommitted(),
                rtMax,
                jvmVersion,
                jvmRuntimeVersion,
                jvmVmName,
                jvmVmVendor,
                jvmSpecVersion,
                jvmArgs);
    }

    private static String jStr(String s) {
        if (s == null) {
            return "null";
        }
        String escaped = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
        return "\"" + escaped + "\"";
    }

    private static String jDbl(Double v) {
        if (v == null || v.isNaN() || v.isInfinite()) {
            return "null";
        }
        return String.format(Locale.US, "%.12g", v);
    }

    /** Nested JSON object for {@code run_manifest} / registry (no outer braces). */
    public String toJsonObjectInner() {
        StringBuilder b = new StringBuilder(384);
        b.append("\"cpu_logical_cores\":").append(cpuLogicalCores).append(',');
        b.append("\"os_name\":").append(jStr(osName)).append(',');
        b.append("\"os_version\":").append(jStr(osVersion)).append(',');
        b.append("\"os_arch\":").append(jStr(osArch)).append(',');
        b.append("\"os_total_memory_bytes\":").append(osTotalMemoryBytes).append(',');
        b.append("\"os_free_memory_bytes\":").append(osFreeMemoryBytes).append(',');
        b.append("\"process_cpu_load\":").append(jDbl(processCpuLoad)).append(',');
        b.append("\"system_cpu_load\":").append(jDbl(systemCpuLoad)).append(',');
        b.append("\"jvm_heap_init_bytes\":").append(jvmHeapInitBytes).append(',');
        b.append("\"jvm_heap_used_bytes\":").append(jvmHeapUsedBytes).append(',');
        b.append("\"jvm_heap_committed_bytes\":").append(jvmHeapCommittedBytes).append(',');
        b.append("\"jvm_heap_max_bytes\":").append(jvmHeapMaxBytes).append(',');
        b.append("\"jvm_nonheap_committed_bytes\":").append(jvmNonHeapCommittedBytes).append(',');
        b.append("\"jvm_runtime_max_memory_bytes\":").append(jvmRuntimeMaxMemoryBytes).append(',');
        b.append("\"jvm_java_version\":").append(jStr(jvmVersion)).append(',');
        b.append("\"jvm_runtime_version\":").append(jStr(jvmRuntimeVersion)).append(',');
        b.append("\"jvm_vm_name\":").append(jStr(jvmVmName)).append(',');
        b.append("\"jvm_vm_vendor\":").append(jStr(jvmVmVendor)).append(',');
        b.append("\"jvm_spec_version\":").append(jStr(jvmSpecVersion)).append(',');
        b.append("\"jvm_input_args\":").append(jStr(jvmInputArgsJoined));
        return b.toString();
    }
}
