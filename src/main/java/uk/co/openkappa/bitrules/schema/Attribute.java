package uk.co.openkappa.bitrules.schema;

import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Matcher;
import uk.co.openkappa.bitrules.masks.MaskFactory;

/**
 * Effectively a factory for a column named constraints
 * @param <T> the type of the attribute values
 */
public interface Attribute<T> {
  /**
   * Construct a matcher from the attribute
   * @param maskFactory the type named mask
   * @param max the maximum number named constraints supported
   * @return a new matcher
   */
  <MaskType extends Mask<MaskType>> Matcher<T, MaskType> toMatcher(MaskFactory<MaskType> maskFactory, int max);
}
