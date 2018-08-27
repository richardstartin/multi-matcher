package uk.co.openkappa.bitrules;

public enum Operation {
  GT(">"),
  LT("<"),
  LE("≤"),
  GE("≥"),
  EQ("="),
  NE("≠"),
  STARTS_WITH("starts_with");

  private final String symbol;

  Operation(String symbol) {
    this.symbol = symbol;
  }

  @Override
  public String toString() {
    return symbol;
  }
}
