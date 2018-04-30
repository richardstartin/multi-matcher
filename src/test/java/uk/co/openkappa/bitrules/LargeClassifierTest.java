package uk.co.openkappa.bitrules;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.co.openkappa.bitrules.config.Schema;

import java.io.IOException;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class LargeClassifierTest {

  @Test
  public void testLargeClassifier() throws IOException {
    Classifier<int[], String> classifier = ImmutableClassifier.
            <Integer, int[], String>definedBy(Schema.<Integer, int[]>newInstance()
                    .withAttribute(0, extract(0))
                    .withAttribute(1, extract(1))
                    .withAttribute(2, extract(2))
                    .withAttribute(3, extract(3))
                    .withAttribute(4, extract(4))
            ).build(() -> IntStream.range(0, 50000)
            .mapToObj(i ->
                    RuleSpecification.of("rule" + i,
                            ImmutableMap.of(
                                    0, Constraint.equalTo(i),
                                    1, Constraint.equalTo(i),
                                    2, Constraint.equalTo(i),
                                    3, Constraint.equalTo(i),
                                    4, Constraint.equalTo(i)
                            ), i, "SEGMENT" + i)

            )
            .map(x -> (RuleSpecification<Integer, String>) x)
            .collect(Collectors.toList())
    );
    int[] vector = new int[]{5, 5, 5, 5, 5};
    String classification = classifier.getBestClassification(vector).orElseThrow(RuntimeException::new);
    assertEquals(classification, "SEGMENT5");
  }


  private static ToIntFunction<int[]> extract(int feature) {
    return features -> features[feature];
  }
}
