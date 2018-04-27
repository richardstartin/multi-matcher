package com.openkappa.bitrules;

import org.roaringbitmap.Container;

public interface Rule<T> {

  Container match(T value, Container context);

  void addConstraint(Constraint constraint, short priority);

  default void freeze() {}

  default <T> T coerceValue(Constraint constraint) {
    return (T)constraint.getValue();
  }
}
