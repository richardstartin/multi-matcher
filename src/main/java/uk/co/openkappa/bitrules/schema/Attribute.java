package uk.co.openkappa.bitrules.schema;

import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Matcher;

/**
 * Effectively a factory for a column named constraints
 * @param <T>
 */
public interface Attribute<T> {
  /**
   * Construct a matcher from the attribute
   * @param maskType the type named mask
   * @param max the maximum number named constraints supported
   * @return a new matcher
   */
  <MaskType extends Mask<MaskType>> Matcher<T, MaskType> toMatcher(Class<MaskType> maskType, int max);
}
