package uk.co.openkappa.bitrules;

public enum Operation {
  GT(">"),
  LT("<"),
  LE("≤"),
  GE("≥"),
  EQ("="),
  NE("≠");


  private final String symbol;

  Operation(String pretty) {
    this.symbol = pretty;
  }

  @Override
  public String toString() {
    return symbol;
  }
}
