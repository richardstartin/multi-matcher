package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.Mask;

import java.util.Collection;
import java.util.function.ToDoubleFunction;

public class SelectivityHeuristics {

  public static <MaskType extends Mask<MaskType>> float avgCardinality(Collection<MaskType> masks) {
    int total = 0;
    for (MaskType mask : masks) {
      total += mask.cardinality();
    }
    return ((float) masks.size()) / total;
  }

  public static <MaskType extends Mask<MaskType>> float avgCardinality(MaskType[] masks) {
    int total = 0;
    for (MaskType mask : masks) {
      total += mask.cardinality();
    }
    return ((float) masks.length) / total;
  }

  public static <Node> float avgCardinality(Collection<Node> nodes, ToDoubleFunction<Node> selectivity) {
    float avg = 0;
    int count = 0;
    for (Node node : nodes) {
      avg += selectivity.applyAsDouble(node);
      ++count;
    }
    return avg / count;
  }

  public static <Node> float avgCardinality(Node[] nodes, ToDoubleFunction<Node> selectivity) {
    float avg = 0;
    int count = 0;
    for (Node node : nodes) {
      avg += selectivity.applyAsDouble(node);
      ++count;
    }
    return avg / count;
  }
}
