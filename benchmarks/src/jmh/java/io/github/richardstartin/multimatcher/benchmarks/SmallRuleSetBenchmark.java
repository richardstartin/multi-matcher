package io.github.richardstartin.multimatcher.benchmarks;

import org.openjdk.jmh.annotations.Benchmark;

public class SmallRuleSetBenchmark {

    @Benchmark
    public String matchingEnum(EnumSchemaMatcherState state) {
        return state.classifier.classification(state.matching).orElse("NA");
    }

    @Benchmark
    public String nonMatchingEnum(EnumSchemaMatcherState state) {
        return state.classifier.classification(state.nonMatching).orElse("NA");
    }

    @Benchmark
    public String matchingString(StringSchemaMatcherState state) {
        return state.classifier.classification(state.matching).orElse("NA");
    }

    @Benchmark
    public String nonMatchingString(StringSchemaMatcherState state) {
        return state.classifier.classification(state.nonMatching).orElse("NA");
    }
}
