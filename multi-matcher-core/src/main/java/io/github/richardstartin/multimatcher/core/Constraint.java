package io.github.richardstartin.multimatcher.core;

import java.util.ArrayList;
import java.util.List;

public class Constraint {

  public static List<Constraint> decompose(Constraint constraint, Operation... operations) {
    List<Constraint> decomposition = new ArrayList<>(operations.length);
    for (Operation operation : operations) {
      decomposition.add(condition(operation, constraint.value));
    }
    return decomposition;
  }

  public static Constraint lessThan(Comparable<?> value) {
    return condition(Operation.LT, value);
  }

  public static Constraint lessThanOrEqualTo(Comparable<?> value) {
    return condition(Operation.LE, value);
  }

  public static Constraint equalTo(Object value) {
    return condition(Operation.EQ, value);
  }

  public static Constraint notEqualTo(Object value) {
    return condition(Operation.NE, value);
  }

  public static Constraint greaterThan(Comparable<?> value) {
    return condition(Operation.GT, value);
  }

  public static Constraint greaterThanOrEqualTo(Comparable<?> value) {
    return condition(Operation.GE, value);
  }

  public static Constraint startsWith(String prefix) {
    return condition(Operation.STARTS_WITH, prefix);
  }

  private static Constraint condition(Operation op, Object value) {
    Constraint rc = new Constraint();
    rc.operation = op;
    rc.value = value;
    return rc;
  }

  private Operation operation;
  private Object value;

  public Constraint() {
  }

  public Operation getOperation() {
    return operation;
  }

  public <T> T getValue() {
    return (T) value;
  }

}
