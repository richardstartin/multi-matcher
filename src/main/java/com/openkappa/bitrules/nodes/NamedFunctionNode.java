package com.openkappa.bitrules.nodes;

import com.openkappa.bitrules.DoubleRelation;
import org.roaringbitmap.Container;

import java.util.function.ToDoubleFunction;

public class NamedFunctionNode<T> {

  private final ToDoubleFunction<T> function;
  private final short priority;
  private final DoubleRelation relation;
  private final double threshold;

  public NamedFunctionNode(ToDoubleFunction<T> function,
                           short priority,
                           DoubleRelation relation,
                           double threshold) {
    this.function = function;
    this.priority = priority;
    this.relation = relation;
    this.threshold = threshold;
  }

  public Container apply(T value, Container scope, Container context) {
    if (scope.contains(priority) && !relation.test(function.applyAsDouble(value), threshold)) {
      return context.remove(priority);
    }
    return context;
  }
}
