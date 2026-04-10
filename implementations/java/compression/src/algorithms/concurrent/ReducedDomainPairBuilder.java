package implementations.java.compression.src.algorithms.concurrent;

import java.util.concurrent.Callable;

import implementations.java.compression.src.utils.fractal.block.GrayBlock;
import implementations.java.compression.src.utils.fractal.block.reductions.ReductionStrategy;
import implementations.java.compression.src.utils.fractal.reducedpair.GrayReducedPair;

public class ReducedDomainPairBuilder implements Callable<GrayReducedPair> {

    private final GrayBlock domainBlock;
    private final int reduceTo;
    private final ReductionStrategy reductionStrategy;

    public ReducedDomainPairBuilder(GrayBlock domainBlock, int reduceTo, ReductionStrategy reductionStrategy) {
        this.domainBlock = domainBlock;
        this.reduceTo = reduceTo;
        this.reductionStrategy = reductionStrategy;
    }

    @Override
    public GrayReducedPair call() {
        GrayBlock reducedBlock = domainBlock.reduce(reduceTo, reductionStrategy);
        return new GrayReducedPair(domainBlock, reducedBlock);
    }
}
