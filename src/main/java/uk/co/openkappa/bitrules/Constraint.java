package uk.co.openkappa.bitrules;

public class Constraint {

  public static Constraint lessThan(Object value) {
    return condition(Operation.LT, value);
  }

  public static Constraint equalTo(Object value) {
    return condition(Operation.EQ, value);
  }

  public static Constraint greaterThan(Object value) {
    return condition(Operation.GT, value);
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

  public Object getValue() {
    return value;
  }

}
