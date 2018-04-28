package uk.co.openkappa.bitrules.config;

import uk.co.openkappa.bitrules.Rule;
import uk.co.openkappa.bitrules.nodes.LongRule;

import java.util.function.ToLongFunction;

public class LongAttribute<T> implements Attribute<T> {

  private final ToLongFunction<T> accessor;

  public LongAttribute(ToLongFunction<T> accessor) {
    this.accessor = accessor;
  }

  @Override
  public Rule<T> toRule() {
    return new LongRule<>(accessor);
  }
}
