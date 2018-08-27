package uk.co.openkappa.bitrules.config;

import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Matcher;
import uk.co.openkappa.bitrules.matchers.IntMatcher;

import java.util.function.ToIntFunction;

/**
 * Creates a column of constraints builder integer semantics
 * @param <T> the type of the classified objects
 */
public class IntAttribute<T> implements Attribute<T> {

  private final ToIntFunction<T> accessor;

  IntAttribute(ToIntFunction<T> accessor) {
    this.accessor = accessor;
  }

  @Override
  public <MaskType extends Mask<MaskType>> Matcher<T, MaskType> toMatcher(Class<MaskType> type, int max) {
    return new IntMatcher<>(accessor, type, max);
  }
}
