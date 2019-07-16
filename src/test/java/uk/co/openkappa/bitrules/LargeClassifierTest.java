package uk.co.openkappa.bitrules;

import org.junit.jupiter.api.Test;
import uk.co.openkappa.bitrules.schema.Schema;

import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class LargeClassifierTest {

  @Test
  public void testLargeClassifier() throws InterruptedException {
    Thread.sleep(10000);
    Classifier<int[], String> classifier = ImmutableClassifier.
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

            .collect(Collectors.toList())
    );
    int[] vector = new int[]{5, 5, 5, 5, 5};
    String classification = classifier.classification(vector).orElseThrow(RuntimeException::new);
    assertEquals(classification, "SEGMENT5");
  }


  private static ToIntFunction<int[]> extract(int feature) {
    return features -> features[feature];
  }
}

