package com.openkappa.bitrules;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Constraint {

  public static Constraint equalTo(String value) {
    Constraint rc = new Constraint();
    rc.operation = Operation.EQ;
    rc.value = value;
    return rc;
  }

  public static Constraint lessThan(Number value) {
    return numericCondition(Operation.LT, value);
  }

  public static Constraint equalTo(Number value) {
    return numericCondition(Operation.EQ, value);
  }

  public static Constraint greaterThan(Number value) {
    return numericCondition(Operation.GT, value);
  }

  private static Constraint numericCondition(Operation op, Number value) {
    Constraint rc = new Constraint();
    rc.operation = op;
    rc.value = value;
    return rc;
  }

  @JsonProperty("op")
  private Operation operation;
  @JsonProperty("value")
  private Object value;

  public Constraint() {
  }

  public Operation getOperation() {
    return operation;
  }

  public Object getValue() {
    return value;
  }

}
