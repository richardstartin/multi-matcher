package uk.co.openkappa.bitrules.config;

import uk.co.openkappa.bitrules.Rule;
import uk.co.openkappa.bitrules.nodes.DoubleRule;

import java.util.function.ToDoubleFunction;

class DoubleAttribute<T> implements Attribute<T> {

  private final ToDoubleFunction<T> accessor;

  public DoubleAttribute(ToDoubleFunction<T> accessor) {
    this.accessor = accessor;
  }

  @Override
  public Rule<T> toRule() {
    return new DoubleRule<>(accessor);
  }
}
