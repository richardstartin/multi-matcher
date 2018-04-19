package com.openkappa.bitrules.config;

import com.openkappa.bitrules.Rule;
import com.openkappa.bitrules.nodes.DoubleRule;

import java.util.function.ToDoubleFunction;

class ConstrainedDoubleAttribute<T> implements ConstrainedAttribute<T> {

  private final ToDoubleFunction<T> accessor;

  public ConstrainedDoubleAttribute(ToDoubleFunction<T> accessor) {
    this.accessor = accessor;
  }

  @Override
  public Rule<T> toRule() {
    return new DoubleRule<>(accessor);
  }
}
