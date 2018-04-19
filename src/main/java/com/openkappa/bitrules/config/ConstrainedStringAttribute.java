package com.openkappa.bitrules.config;

import com.openkappa.bitrules.Rule;
import com.openkappa.bitrules.nodes.StringRule;

import java.util.function.Function;

class ConstrainedStringAttribute<T> implements ConstrainedAttribute<T> {

  private final Function<T, String> accessor;

  public ConstrainedStringAttribute(Function<T, String> accessor) {
    this.accessor = accessor;
  }

  @Override
  public Rule<T> toRule() {
    return new StringRule<>(accessor);
  }
}
