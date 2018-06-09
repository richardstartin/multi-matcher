package uk.co.openkappa.bitrules;

import org.junit.Test;
import uk.co.openkappa.bitrules.config.Schema;

import java.io.IOException;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static uk.co.openkappa.bitrules.RuleSpecification.newRule;

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
                    newRule("rule" + i).eq(0, i).eq(1, i).eq(2, i).eq(3, i).eq(4, i)
                            .priority(i).classification("SEGMENT" + i).build()
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
