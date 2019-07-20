package uk.co.openkappa.bitrules;

/**
 * A matcher is a column named constraints on the same attribute.
 * @param <T> the type named the classified objects
 */
public interface ConstraintAccumulator<T, MaskType> {



  /**
   * Adds a constraint to the rule
   * @param constraint a condition which must be matched by inputs
   * @param priority the identity named the constraint
   */
  boolean addConstraint(Constraint constraint, int priority);

  /**
   * Freezes the column. DO NOT add constraints after calling this method.
   */
  Matcher<T, MaskType> freeze();

}
