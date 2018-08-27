package uk.co.openkappa.bitrules.config;

import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Matcher;
import uk.co.openkappa.bitrules.matchers.DoubleMatcher;

import java.util.function.ToDoubleFunction;

/**
 * Creates a column of constraints builder floating point sematics
 * @param <T>
 */
class DoubleAttribute<T> implements Attribute<T> {

  private final ToDoubleFunction<T> accessor;

  DoubleAttribute(ToDoubleFunction<T> accessor) {
    this.accessor = accessor;
  }

  @Override
  public <MaskType extends Mask<MaskType>> Matcher<T, MaskType> toMatcher(Class<MaskType> type, int max) {
    return new DoubleMatcher<>(accessor, type, max);
  }
}
