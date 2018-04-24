package com.openkappa.bitrules.nodes;

import com.openkappa.bitrules.Constraint;
import com.openkappa.bitrules.Rule;
import org.roaringbitmap.Container;

import java.util.function.Function;

public class StringRule<T> implements Rule<T> {

  private final Function<T, String> accessor;
  private final StringEqualityNode rules;

  public StringRule(Function<T, String> accessor) {
    this.accessor = accessor;
    this.rules = new StringEqualityNode();
  }

  public Container match(T value, Container context) {
    return rules.apply(accessor.apply(value), context);
  }

  @Override
  public void addConstraint(Constraint constraint, short priority) {
    rules.add(mustGetStringValue(constraint), priority);
  }
}
