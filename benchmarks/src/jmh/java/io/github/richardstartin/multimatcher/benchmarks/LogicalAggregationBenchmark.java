package io.github.richardstartin.multimatcher.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jol.info.ClassData;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.FieldLayout;
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

        @Param({"0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                "10", "11", "12", "13", "14", "15", "16", "17", "18",
                "19", "20", "21", "22", "23", "24", "25", "26", "27", "28",
                "29", "30", "31", "32"})
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
            System.out.println(ClassLayout.parseInstance(source).toPrintable());
            System.out.println(GraphLayout.parseInstance(source).toPrintable());
            System.out.println(ClassLayout.parseInstance(gap).toPrintable());
            System.out.println(GraphLayout.parseInstance(gap).toPrintable());
            System.out.println(ClassLayout.parseInstance(target).toPrintable());
            System.out.println(GraphLayout.parseInstance(target).toPrintable());
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
