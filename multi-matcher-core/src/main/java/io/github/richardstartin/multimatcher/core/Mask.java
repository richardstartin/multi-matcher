package io.github.richardstartin.multimatcher.core;

import java.util.stream.IntStream;

public interface Mask<T extends Mask<T>> {

  static <U extends Mask<U>> U with(U mask, int priority) {
    mask.add(priority);
    return mask;
  }

  static <U extends Mask<U>> U without(U mask, int priority) {
    mask.remove(priority);
    return mask;
  }

  void add(int id);
  void remove(int id);

  T inPlaceAndNot(T other);
  T inPlaceAnd(T other);
  T inPlaceOr(T other);
  T resetTo(Mask<T> other);
  void clear();
  T unwrap();
  IntStream stream();
  int first();
  T clone();
  void optimise();
  boolean isEmpty();
  int cardinality();
  default T and(T other) {
    return clone().inPlaceAnd(other);
  }
  default T andNot(T other) {
    return clone().inPlaceAndNot(other);
  }
  default T or(T other) {
    return clone().inPlaceOr(other);
  }

}
