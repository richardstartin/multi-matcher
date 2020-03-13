package io.github.richardstartin.multimatcher.core.schema;

import io.github.richardstartin.multimatcher.core.ConstraintAccumulator;
import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.masks.MaskFactory;
import io.github.richardstartin.multimatcher.core.matchers.StringConstraintAccumulator;

import java.util.function.Function;

public class StringAttribute<Input> implements Attribute<Input> {

  private final Function<Input, String> accessor;

  public StringAttribute(Function<Input, String> accessor) {
    this.accessor = accessor;
  }

  @Override
  public <MaskType extends Mask<MaskType>> ConstraintAccumulator<Input, MaskType> newAccumulator(MaskFactory<MaskType> maskFactory, int max) {
    return new StringConstraintAccumulator<>(accessor, maskFactory, max);
  }
}
