package uk.co.openkappa.bitrules.config;

import uk.co.openkappa.bitrules.Rule;
import uk.co.openkappa.bitrules.nodes.DoubleRule;

import java.util.function.ToDoubleFunction;

/**
 * Creates a column of constraints with floating point sematics
 * @param <T>
 */
class DoubleAttribute<T> implements Attribute<T> {

  private final ToDoubleFunction<T> accessor;

  DoubleAttribute(ToDoubleFunction<T> accessor) {
    this.accessor = accessor;
  }

  @Override
  public Rule<T> toRule() {
    return new DoubleRule<>(accessor);
  }
}
