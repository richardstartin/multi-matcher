package uk.co.openkappa.bitrules.config;

import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Matcher;

/**
 * Effectively a factory for a column of constraints
 * @param <T>
 */
public interface Attribute<T> {
  /**
   * Construct a matcher from the attribute
   * @param maskType the type of mask
   * @param max the maximum number of constraints supported
   * @return a new matcher
   */
  <MaskType extends Mask<MaskType>> Matcher<T, MaskType> toMatcher(Class<MaskType> maskType, int max);
}
