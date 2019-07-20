package uk.co.openkappa.bitrules.schema;

import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.ConstraintAccumulator;
import uk.co.openkappa.bitrules.masks.MaskFactory;
import uk.co.openkappa.bitrules.matchers.GenericConstraintAccumulator;

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
  public <MaskType extends Mask<MaskType>> ConstraintAccumulator<Input, MaskType> toMatcher(MaskFactory<MaskType> maskFactory, int max) {
    return new GenericConstraintAccumulator<>(() -> new EnumMap<>(type), accessor, maskFactory, max);
  }
}
