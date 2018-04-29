package uk.co.openkappa.bitrules;

import org.roaringbitmap.Container;

/**
 * A rule is a column of constraints on the same attribute.
 * @param <T> the type of the classified objects
 */
public interface Rule<T> {

  /**
   * Returns the identities of all of the constraints which are satisfied bt the value,
   * so long as they have not already been invalidated by prior mismatches on other attributes
   * @param value the value to match
   * @param context the identities of constraints satisfied prior to the match
   * @return the identities of all constrainst still satisfied
   */
  Container match(T value, Container context);

  /**
   * Adds a constraint to the rule
   * @param constraint a condition which must be matched by inputs
   * @param priority the identity of the constraint
   */
  void addConstraint(Constraint constraint, short priority);

  /**
   * Freezes the column. DO NOT add constraints after calling this, in case this
   * is overridden.
   */
  default void freeze() {}

  default <U> U coerceValue(Constraint constraint) {
    return (U)constraint.getValue();
  }
}
