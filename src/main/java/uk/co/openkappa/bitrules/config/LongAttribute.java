package uk.co.openkappa.bitrules.config;

import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Matcher;
import uk.co.openkappa.bitrules.matchers.LongMatcher;

import java.util.function.ToLongFunction;

/**
 * Creates a column of constraints builder long semantics
 * @param <T> the type of the classified objects
 */
public class LongAttribute<T> implements Attribute<T> {

  private final ToLongFunction<T> accessor;

  LongAttribute(ToLongFunction<T> accessor) {
    this.accessor = accessor;
  }

  @Override
  public <MaskType extends Mask<MaskType>> Matcher<T, MaskType> toMatcher(Class<MaskType> type, int max) {
    return new LongMatcher<>(accessor, type, max);
  }
}
