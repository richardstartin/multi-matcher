package io.github.richardstartin.multimatcher.core.schema;

import io.github.richardstartin.multimatcher.core.ConstraintAccumulator;
import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.masks.MaskFactory;
import io.github.richardstartin.multimatcher.core.matchers.GenericConstraintAccumulator;

import java.util.EnumMap;
import java.util.function.Function;

public class EnumAttribute<E extends Enum<E>, Input> implements Attribute<Input> {

  private final Function<Input, E> accessor;
  private final Class<E> type;

  public EnumAttribute(Class<E> type, Function<Input, E> accessor) {
    this.accessor = accessor;
    this.type = type;
  }

  @Override
  public <MaskType extends Mask<MaskType>>
  ConstraintAccumulator<Input, MaskType> newAccumulator(MaskFactory<MaskType> maskFactory, int max) {
    return new GenericConstraintAccumulator<>(() -> new EnumMap<>(type), accessor, maskFactory, max);
  }
}
