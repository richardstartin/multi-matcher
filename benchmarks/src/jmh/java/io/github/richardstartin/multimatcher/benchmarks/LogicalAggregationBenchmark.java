package io.github.richardstartin.multimatcher.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.management.ThreadMXBean;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Check if aggregation of an array at an offset vectorises or not
 */
@State(Scope.Benchmark)
public class LogicalAggregationBenchmark {


    @Param("256")
    int targetSize;

    @Param("1024")
    int sourceSize;

    @Param({"0", "256", "512", "768"})
    int offset;

    private long[] source;
    private long[] target;


    @Setup(Level.Trial)
    public void setup() {
        source = new long[sourceSize];
        target = new long[targetSize];
        for (int i = 0; i < source.length; ++i) {
            source[i] = ThreadLocalRandom.current().nextLong();
        }
        for (int i = 0; i < target.length; ++i) {
            target[i] = ThreadLocalRandom.current().nextLong();
        }
    }

    @Benchmark
    public void intersection(Blackhole bh) {
        for (int i = 0; i < target.length; ++i) {
            target[i] &= source[offset + i];
        }
        bh.consume(target);
    }


    @Benchmark
    public void intersectionNoOffset(Blackhole bh) {
        for (int i = 0; i < target.length; ++i) {
            target[i] &= source[i];
        }
        bh.consume(target);
    }
}
