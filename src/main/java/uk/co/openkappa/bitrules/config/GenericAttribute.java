package uk.co.openkappa.bitrules.config;

import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Matcher;
import uk.co.openkappa.bitrules.matchers.GenericMatcher;

import java.util.function.Function;

/**
 * Creates a column of constraints with equality semantics only
 * @param <T> the type of the classified objects
 * @param <U> the type of the attribute
 */
class GenericAttribute<T, U> implements Attribute<T> {

  private final Function<T, U> accessor;

  GenericAttribute(Function<T, U> accessor) {
    this.accessor = accessor;
  }

  @Override
  public <MaskType extends Mask<MaskType>> Matcher<T, MaskType> toMatcher(Class<MaskType> type) {
    return new GenericMatcher<>(accessor, type);
  }
}
