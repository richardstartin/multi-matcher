package io.github.richardstartin.multimatcher.core;

public enum Operation {
    GT(">"),
    LT("<"),
    LE("≤"),
    GE("≥"),
    EQ("="),
    NE("≠"),
    STARTS_WITH("starts_with");

    public static int SIZE = values().length;

    private final String symbol;

    Operation(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
