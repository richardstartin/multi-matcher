package com.openkappa.bitrules.config;

import com.openkappa.bitrules.Rule;
import com.openkappa.bitrules.nodes.GenericRule;

import java.util.function.Function;

class GenericAttribute<T, U> implements Attribute<T> {

  private final Function<T, U> accessor;

  public GenericAttribute(Function<T, U> accessor) {
    this.accessor = accessor;
  }

  @Override
  public Rule<T> toRule() {
    return new GenericRule<>(accessor);
  }
}
