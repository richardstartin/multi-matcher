package io.github.richardstartin.multimatcher.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jol.info.GraphLayout;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Check if aggregation of an array at an offset vectorises or not
 */
public class LogicalAggregationBenchmark {


    @State(Scope.Benchmark)
    public static class BaseState {

        byte[] gap;

        @Param("256")
        int targetSize;

        @Param("1024")
        int sourceSize;

        @Param({"0", "8", "16", "24", "32", "40", "48", "56", "64", "72", "80", "88", "96", "104", "112", "120" })
        int padding;

        long[] source;
        long[] target;


        @Setup(Level.Trial)
        public void setup() {
            source = new long[sourceSize];
            gap = new byte[padding];
            target = new long[targetSize];
            fill(source);
            fill(target);
            System.out.println("source:" + Long.toHexString(GraphLayout.parseInstance(source).startAddress()));
            System.out.println("gap:" + Long.toHexString(GraphLayout.parseInstance(gap).startAddress()));
            System.out.println("target:" + Long.toHexString(GraphLayout.parseInstance(target).startAddress()));
        }

        private static void fill(long[] data) {
            for (int i = 0; i < data.length; ++i) {
                data[i] = ThreadLocalRandom.current().nextLong();
            }
        }

    }

    public static class ConstantOffset256State extends BaseState {

        private static final int offset = 256;
    }

    public static class ConstantOffset0State extends BaseState {

        private static final int offset = 0;
    }

    public static class DynamicOffsetState extends BaseState {
        @Param({"0", "256", "512", "768"})
        int offset;
    }


    @Benchmark
    public void intersectionWithOffset(DynamicOffsetState state, Blackhole bh) {
        var target = state.target;
        var source = state.source;
        int offset = state.offset;
        for (int i = 0; i < state.target.length; ++i) {
            target[i] &= source[offset + i];
        }
        bh.consume(target);
    }


    @Benchmark
    public void intersectionWithConstantOffset256(BaseState state, Blackhole bh) {
        var target = state.target;
        var source = state.source;
        for (int i = 0; i < state.target.length; ++i) {
            target[i] &= source[ConstantOffset256State.offset + i];
        }
        bh.consume(target);
    }

    @Benchmark
    public void intersectionWithConstantOffset0(BaseState state, Blackhole bh) {
        var target = state.target;
        var source = state.source;
        for (int i = 0; i < state.target.length; ++i) {
            target[i] &= source[ConstantOffset0State.offset + i];
        }
        bh.consume(target);
    }


    @Benchmark
    public void intersectionNoOffset(BaseState state, Blackhole bh) {
        var target = state.target;
        var source = state.source;
        for (int i = 0; i < state.target.length; ++i) {
            target[i] &= source[i];
        }
        bh.consume(target);
    }
}
