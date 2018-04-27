package com.openkappa.bitrules.nodes;

import com.openkappa.bitrules.Constraint;
import com.openkappa.bitrules.Rule;
import org.roaringbitmap.Container;

import java.util.function.Function;

public class GenericRule<T, U> implements Rule<T> {

  private final Function<T, U> accessor;
  private final GenericEqualityNode<U> rules;

  public GenericRule(Function<T, U> accessor) {
    this.accessor = accessor;
    this.rules = new GenericEqualityNode<>();
  }

  public Container match(T value, Container context) {
    return rules.apply(accessor.apply(value), context);
  }

  @Override
  public void addConstraint(Constraint constraint, short priority) {
    rules.add((U) constraint.getValue(), priority);
  }
}
