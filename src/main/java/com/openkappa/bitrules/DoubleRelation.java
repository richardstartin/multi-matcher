package com.openkappa.bitrules;

public enum DoubleRelation implements DoubleBiPredicate {
  EQ("=", (x, y) -> Math.abs(x - y) < 1E-8),
  LT("<", (x, y) -> x < y),
  GT(">", (x, y) -> x > y);

  private final DoubleBiPredicate delegate;
  private final String name;

  DoubleRelation(String prettyName, DoubleBiPredicate delegate) {
    this.delegate = delegate;
    this.name = prettyName;
  }

  public static DoubleRelation from(Operation operation) {
    if (null != operation) {
      switch (operation) {
        case LE:
        case LT:
          return LT;
        case GE:
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
  public boolean test(double left, double right) {
    return delegate.test(left, right);
  }

  @Override
  public String toString() {
    return name;
  }
}
