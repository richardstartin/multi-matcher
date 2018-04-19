package com.openkappa.bitrules;

public enum IntRelation implements IntBiPredicate {
  EQ("=", (x, y) -> x == y),
  LT("<", (x, y) -> x < y),
  LE("<=", (x, y) -> x <= y),
  GT(">", (x, y) -> x > y),
  GE(">=", (x, y) -> x >= y);

  private final IntBiPredicate delegate;
  private final String name;

  IntRelation(String prettyName, IntBiPredicate delegate) {
    this.delegate = delegate;
    this.name = prettyName;
  }

  public static IntRelation from(Operation operation) {
    if (null != operation) {
      switch (operation) {
        case LE:
          return LE;
        case LT:
          return LT;
        case GE:
          return GE;
        case GT:
          return GT;
        case EQ:
          return EQ;
        default:
      }
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean test(int left, int right) {
    return delegate.test(left, right);
  }

  @Override
  public String toString() {
    return name;
  }
}
