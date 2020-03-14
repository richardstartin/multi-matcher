package io.github.richardstartin.multimatcher.core;

import io.github.richardstartin.multimatcher.core.schema.Schema;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class LargeClassifierTest {

    private static ToIntFunction<int[]> extract(int feature) {
        return features -> features[feature];
    }

    @Test
    public void testLargeClassifier() {
        Classifier<int[], String> classifier = Classifier.
                <Integer, int[], String>builder(Schema.<Integer, int[]>create()
                        .withAttribute(0, extract(0))
                        .withAttribute(1, extract(1))
                        .withAttribute(2, extract(2))
                        .withAttribute(3, extract(3))
                        .withAttribute(4, extract(4))
                ).build(IntStream.range(0, 50000)
                .mapToObj(i ->
                        MatchingConstraint.<Integer, String>anonymous()
                                .eq(0, i)
                                .eq(1, i)
                                .eq(2, i)
                                .eq(3, i)
                                .eq(4, i)
                                .classification("SEGMENT" + i)
                                .build())

                .collect(toList())
        );
        int[] vector = new int[]{5, 5, 5, 5, 5};
        String classification = classifier.classification(vector).orElseThrow(RuntimeException::new);
        assertEquals(classification, "SEGMENT5");
    }

    @Test
    public void testLargeDiscreteClassifier() {
        Classifier<Map<String, Object>, String> classifier = Classifier.
                <String, Map<String, Object>, String>builder(Schema.<String, Map<String, Object>>create()
                        .withStringAttribute("attr1", (Map<String, Object> map) -> (String) map.get("attr1"))
                        .withStringAttribute("attr2", (Map<String, Object> map) -> (String) map.get("attr2"))
                        .withStringAttribute("attr3", (Map<String, Object> map) -> (String) map.get("attr3"))
                        .withStringAttribute("attr4", (Map<String, Object> map) -> (String) map.get("attr4"))
                        .withStringAttribute("attr5", (Map<String, Object> map) -> (String) map.get("attr5"))
                        .withStringAttribute("attr6", (Map<String, Object> map) -> (String) map.get("attr6"))
                ).build(IntStream.range(0, 50000)
                .mapToObj(i -> MatchingConstraint.<String, String>anonymous()
                        .eq("attr1", "value" + (i / 10000))
                        .eq("attr2", "value" + (i / 1000))
                        .eq("attr3", "value" + (i / 500))
                        .eq("attr4", "value" + (i / 250))
                        .eq("attr5", "value" + (i / 100))
                        .eq("attr6", "value" + (i / 10))
                        .classification("SEGMENT" + i).build()
                ).collect(toList()));

        Map<String, Object> msg = new HashMap<>();
        msg.put("attr1", "value0");
        msg.put("attr2", "value0");
        msg.put("attr3", "value0");
        msg.put("attr4", "value0");
        msg.put("attr5", "value0");
        msg.put("attr6", "value9");

        String classification = classifier.classification(msg).orElseThrow(RuntimeException::new);
        assertEquals("SEGMENT90", classification);
    }
}

