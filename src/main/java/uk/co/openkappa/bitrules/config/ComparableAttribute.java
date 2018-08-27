package uk.co.openkappa.bitrules.config;

import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Matcher;
import uk.co.openkappa.bitrules.matchers.ComparableMatcher;

import java.util.Comparator;
import java.util.function.Function;

/**
 * Creates a column of constraints builder equality and order semantics
 * @param <T> the type of the classified objects
 * @param <U> the type of the attribute
 */
public class ComparableAttribute<T, U> implements Attribute<T> {

  private final Comparator<U> comparator;
  private final Function<T, U> accessor;

  ComparableAttribute(Comparator<U> comparator, Function<T, U> accessor) {
    this.comparator = comparator;
    this.accessor = accessor;
  }

  @Override
  public <MaskType extends Mask<MaskType>> Matcher<T, MaskType> toMatcher(Class<MaskType> type, int max) {
    return new ComparableMatcher<>(accessor, comparator, type, max);
  }
}
