package uk.co.openkappa.bitrules.config;

import uk.co.openkappa.bitrules.Rule;

/**
 * Effectively a factory for a column of constraints
 * @param <T>
 */
public interface Attribute<T> {
  /**
   * Construct a rule from the attribute
   * @return a new rule instance
   */
  Rule<T> toRule();
}
