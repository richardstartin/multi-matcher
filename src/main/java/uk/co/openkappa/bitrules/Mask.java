package uk.co.openkappa.bitrules;

import java.util.stream.IntStream;

public interface Mask<T extends Mask> {

  static <U extends Mask> U with(U mask, int priority) {
    mask.add(priority);
    return mask;
  }

  static <U extends Mask> U without(U mask, int priority) {
    mask.remove(priority);
    return mask;
  }

  void add(int id);
  void remove(int id);
  T and(T other);
  T andNot(T other);
  T or(T other);
  T inPlaceAnd(T other);
  T inPlaceOr(T other);
  IntStream stream();
  int first();
  T clone();
  void optimise();
  boolean isEmpty();
  int cardinality();

}
