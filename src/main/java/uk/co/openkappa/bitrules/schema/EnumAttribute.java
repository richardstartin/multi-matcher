package uk.co.openkappa.bitrules.schema;

import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Matcher;
import uk.co.openkappa.bitrules.matchers.GenericMatcher;

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
  public <MaskType extends Mask<MaskType>> Matcher<Input, MaskType> toMatcher(Class<MaskType> maskType, int max) {
    return new GenericMatcher<>(() -> new EnumMap<>(type), accessor, maskType, max);
  }
}
