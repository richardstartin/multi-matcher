package com.openkappa.bitrules;

public enum LongRelation implements LongBiPredicate {
  EQ("=", (x, y) -> x == y),
  LT("<", (x, y) -> x < y),
  LE("<=", (x, y) -> x <= y),
  GT(">", (x, y) -> x > y),
  GE(">=", (x, y) -> x >= y);

  private final LongBiPredicate delegate;
  private final String name;

  LongRelation(String prettyName, LongBiPredicate delegate) {
    this.delegate = delegate;
    this.name = prettyName;
  }

  public static LongRelation from(Operation operation) {
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
        throw new IllegalStateException("Unknown operation " + operation);
    }
  }

  @Override
  public boolean test(long left, long right) {
    return delegate.test(left, right);
  }

  @Override
  public String toString() {
    return name;
  }
}
