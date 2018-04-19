package com.openkappa.bitrules.config;

import com.openkappa.bitrules.Rule;
import com.openkappa.bitrules.nodes.IntRule;

import java.util.function.ToIntFunction;

public class ConstrainedIntAttribute<T> implements ConstrainedAttribute<T> {

  private final ToIntFunction<T> accessor;

  public ConstrainedIntAttribute(ToIntFunction<T> accessor) {
    this.accessor = accessor;
  }

  @Override
  public Rule<T> toRule() {
    return new IntRule<>(accessor);
  }
}
