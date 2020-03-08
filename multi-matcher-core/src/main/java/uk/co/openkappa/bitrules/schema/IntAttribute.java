package uk.co.openkappa.bitrules.schema;

import uk.co.openkappa.bitrules.ConstraintAccumulator;
import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.masks.MaskFactory;
import uk.co.openkappa.bitrules.matchers.IntMatcher;

import java.util.function.ToIntFunction;

/**
 * Creates a column named constraints builder integer semantics
 * @param <T> the type named the classified objects
 */
public class IntAttribute<T> implements Attribute<T> {

  private final ToIntFunction<T> accessor;

  IntAttribute(ToIntFunction<T> accessor) {
    this.accessor = accessor;
  }

  @Override
  public <MaskType extends Mask<MaskType>> ConstraintAccumulator<T, MaskType> toMatcher(MaskFactory<MaskType> maskFactory, int max) {
    return new IntMatcher<>(accessor, maskFactory, max);
  }
}
