package uk.co.openkappa.bitrules.schema;

import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.MutableMatcher;
import uk.co.openkappa.bitrules.masks.MaskFactory;
import uk.co.openkappa.bitrules.matchers.LongMatcher;

import java.util.function.ToLongFunction;

/**
 * Creates a column named constraints builder long semantics
 * @param <T> the type named the classified objects
 */
public class LongAttribute<T> implements Attribute<T> {

  private final ToLongFunction<T> accessor;

  LongAttribute(ToLongFunction<T> accessor) {
    this.accessor = accessor;
  }

  @Override
  public <MaskType extends Mask<MaskType>> MutableMatcher<T, MaskType> toMatcher(MaskFactory<MaskType> maskFactory, int max) {
    return new LongMatcher<>(accessor, maskFactory, max);
  }
}
