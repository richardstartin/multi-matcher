package uk.co.openkappa.bitrules.config;

import uk.co.openkappa.bitrules.Rule;
import uk.co.openkappa.bitrules.nodes.IntRule;

import java.util.function.ToIntFunction;

public class IntAttribute<T> implements Attribute<T> {

  private final ToIntFunction<T> accessor;

  public IntAttribute(ToIntFunction<T> accessor) {
    this.accessor = accessor;
  }

  @Override
  public Rule<T> toRule() {
    return new IntRule<>(accessor);
  }
}
