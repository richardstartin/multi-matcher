package com.openkappa.bitrules;


import com.openkappa.bitrules.config.RuleValueTypeMismatch;
import org.roaringbitmap.Container;

public interface Rule<T> {

  Container match(T value, Container context);

  void addConstraint(Constraint constraint, short priority);

  default void freeze() {}

  default double mustGetDoubleValue(Constraint constraint) {
    Object value = constraint.getValue();
    if (value instanceof Number) {
      return ((Number) value).doubleValue();
    }
    throw new RuleValueTypeMismatch("Rule setup error. Expected a value of a numeric type but got "
            + null == value ? "null" : value.getClass().getCanonicalName());
  }

  default String mustGetStringValue(Constraint constraint) {
    Object value = constraint.getValue();
    if (value instanceof String) {
      return (String) value;
    }
    throw new RuleValueTypeMismatch("Rule setup error. Expected a value of a String but got "
            + null == value ? "null" : value.getClass().getCanonicalName());
  }
}
