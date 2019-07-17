package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.Mask;

import java.util.Collection;
import java.util.function.ToDoubleFunction;

public class SelectivityHeuristics {

  static <MaskType extends Mask<MaskType>> float avgCardinality(Collection<MaskType> masks) {
    int total = 0;
    for (MaskType mask : masks) {
      total += mask.cardinality();
    }
    return ((float) masks.size()) / total;
  }

  static <MaskType extends Mask<MaskType>> float avgCardinality(MaskType[] masks) {
    int total = 0;
    for (MaskType mask : masks) {
      total += mask.cardinality();
    }
    return ((float) masks.length) / total;
  }

  static <Node> float avgCardinality(Collection<Node> nodes, ToDoubleFunction<Node> selectivity) {
    float avg = 0;
    int count = 0;
    for (var node : nodes) {
      avg += selectivity.applyAsDouble(node);
      ++count;
    }
    return avg / count;
  }
}
