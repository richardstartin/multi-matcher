package io.github.richardstartin.multimatcher.benchmarks;

import io.github.richardstartin.multimatcher.core.Classifier;
import io.github.richardstartin.multimatcher.core.MatchingConstraint;
import io.github.richardstartin.multimatcher.core.schema.Schema;
import org.openjdk.jmh.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@State(Scope.Benchmark)
public class LargeClassifierBenchmark {


    @Param({"15000", "20000", "50000"})
    int size;


    private Map<String, Object> message;
    private Classifier<Map<String, Object>, String> classifier;

    @Setup(Level.Trial)
    public void init() {
        this.message = message();
        this.classifier = largeDiscreteClassifier(size);
    }

    @Benchmark
    public String match() {
        return classifier.classificationOrNull(message);
    }

    public static Map<String, Object> message() {
        Map<String, Object> msg = new HashMap<>();
        msg.put("attr1", "value0");
        msg.put("attr2", "value0");
        msg.put("attr3", "value0");
        msg.put("attr4", "value0");
        msg.put("attr5", "value0");
        msg.put("attr6", "value9");
        return msg;
    }

    public static Classifier<Map<String, Object>, String> largeDiscreteClassifier(int size) {
        return Classifier.
                <String, Map<String, Object>, String>builder(Schema.<String, Map<String, Object>>create()
                        .withStringAttribute("attr1", (Map<String, Object> map) -> (String)map.get("attr1"))
                        .withStringAttribute("attr2", (Map<String, Object> map) -> (String)map.get("attr2"))
                        .withStringAttribute("attr3", (Map<String, Object> map) -> (String)map.get("attr3"))
                        .withStringAttribute("attr4", (Map<String, Object> map) -> (String)map.get("attr4"))
                        .withStringAttribute("attr5", (Map<String, Object> map) -> (String)map.get("attr5"))
                        .withStringAttribute("attr6", (Map<String, Object> map) -> (String)map.get("attr6"))
                )
                .useDirectBuffers(true)
                .withOptimisedStorageSpace(100 * 1024 * 1024)
                .build(IntStream.range(0, size)
                .mapToObj(i -> MatchingConstraint.<String, String>anonymous()
                        .eq("attr1", "value" + (i / 10000))
                        .eq("attr2", "value" + (i / 1000))
                        .eq("attr3", "value" + (i / 500))
                        .eq("attr4", "value" + (i / 250))
                        .eq("attr5", "value" + (i / 100))
                        .eq("attr6", "value" + (i / 10))
                        .classification("SEGMENT" + i).build()
                ).collect(toList()));

    }
}
