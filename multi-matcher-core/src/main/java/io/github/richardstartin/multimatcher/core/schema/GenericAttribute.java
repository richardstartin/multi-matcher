package io.github.richardstartin.multimatcher.core.schema;

import io.github.richardstartin.multimatcher.core.ConstraintAccumulator;
import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.masks.MaskFactory;
import io.github.richardstartin.multimatcher.core.matchers.GenericConstraintAccumulator;

import java.util.HashMap;
import java.util.function.Function;

/**
 * Creates a column named constraints builder equality semantics only
 * @param <T> the type named the classified objects
 * @param <U> the type named the attribute
 */
class GenericAttribute<T, U> implements Attribute<T> {

  private final Function<T, U> accessor;

  GenericAttribute(Function<T, U> accessor) {
    this.accessor = accessor;
  }

  @Override
  public <MaskType extends Mask<MaskType>> ConstraintAccumulator<T, MaskType> newAccumulator(MaskFactory<MaskType> maskFactory, int max) {
    return new GenericConstraintAccumulator<>(HashMap::new, accessor, maskFactory, max);
  }
}