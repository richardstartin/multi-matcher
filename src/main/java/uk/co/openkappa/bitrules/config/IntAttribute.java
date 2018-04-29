package uk.co.openkappa.bitrules.config;

import uk.co.openkappa.bitrules.Rule;
import uk.co.openkappa.bitrules.nodes.IntRule;

import java.util.function.ToIntFunction;

/**
 * Creates a column of constraints with integer semantics
 * @param <T> the type of the classified objects
 */
public class IntAttribute<T> implements Attribute<T> {

  private final ToIntFunction<T> accessor;

  IntAttribute(ToIntFunction<T> accessor) {
    this.accessor = accessor;
  }

  @Override
  public Rule<T> toRule() {
    return new IntRule<>(accessor);
  }
}
