package com.openkappa.bitrules.config;

import com.openkappa.bitrules.Rule;
import com.openkappa.bitrules.nodes.LongRule;

import java.util.function.ToLongFunction;

public class ConstrainedLongAttribute<T> implements ConstrainedAttribute<T> {

  private final ToLongFunction<T> accessor;

  public ConstrainedLongAttribute(ToLongFunction<T> accessor) {
    this.accessor = accessor;
  }

  @Override
  public Rule<T> toRule() {
    return new LongRule<>(accessor);
  }
}
