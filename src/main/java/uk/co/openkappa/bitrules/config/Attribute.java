package uk.co.openkappa.bitrules.config;

import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Matcher;

/**
 * Effectively a factory for a column of constraints
 * @param <T>
 */
public interface Attribute<T> {
  /**
   * Construct a rule from the attribute
   * @return a new rule instance
   */
  <MaskType extends Mask<MaskType>> Matcher<T, MaskType> toMatcher(Class<MaskType> maskType);
}
