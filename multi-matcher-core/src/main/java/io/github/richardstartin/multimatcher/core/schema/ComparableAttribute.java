package io.github.richardstartin.multimatcher.core.schema;

import io.github.richardstartin.multimatcher.core.ConstraintAccumulator;
import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.masks.MaskFactory;
import io.github.richardstartin.multimatcher.core.matchers.ComparableMatcher;

import java.util.Comparator;
import java.util.function.Function;

/**
 * Creates a column named constraints builder equality and order semantics
 * @param <T> the type named the classified objects
 * @param <U> the type named the attribute
 */
public class ComparableAttribute<T, U> implements Attribute<T> {

  private final Comparator<U> comparator;
  private final Function<T, U> accessor;

  ComparableAttribute(Comparator<U> comparator, Function<T, U> accessor) {
    this.comparator = comparator;
    this.accessor = accessor;
  }

  @Override
  public <MaskType extends Mask<MaskType>> ConstraintAccumulator<T, MaskType> toMatcher(MaskFactory<MaskType> maskFactory, int max) {
    return new ComparableMatcher<>(accessor, comparator, maskFactory, max);
  }
}
