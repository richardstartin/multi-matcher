package io.github.richardstartin.multimatcher.core;

public enum Operation {
  GT(">"),
  LT("<"),
  LE("≤"),
  GE("≥"),
  EQ("="),
  NE("≠"),
  STARTS_WITH("starts_with");



  static Operation[] VALUES = values();
  public static int SIZE = VALUES.length;

  public static Operation valueOf(int ordinal) {
    return VALUES[ordinal];
  }

  private final String symbol;

  Operation(String symbol) {
    this.symbol = symbol;
  }

  @Override
  public String toString() {
    return symbol;
  }
}
