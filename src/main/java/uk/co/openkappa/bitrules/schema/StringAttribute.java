package uk.co.openkappa.bitrules.schema;

import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Matcher;
import uk.co.openkappa.bitrules.matchers.StringMatcher;

import java.util.function.Function;

public class StringAttribute<Input> implements Attribute<Input> {

  private final Function<Input, String> accessor;

  public StringAttribute(Function<Input, String> accessor) {
    this.accessor = accessor;
  }

  @Override
  public <MaskType extends Mask<MaskType>> Matcher<Input, MaskType> toMatcher(Class<MaskType> maskType, int max) {
    return new StringMatcher<>(accessor, maskType, max);
  }
}
