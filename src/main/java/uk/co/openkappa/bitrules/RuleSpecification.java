package uk.co.openkappa.bitrules;

import java.util.Map;


public class RuleSpecification<Key, Classification> {

  public static <Key, Classification> RuleSpecification of(String id,
                                                           Map<Key, Constraint> constraints,
                                                           int priority,
                                                           Classification classification) {
    return new RuleSpecification<>(id, constraints, priority, classification);
  }

  private String id;
  private Map<Key, Constraint> constraints;
  private int priority;
  private Classification classification;

  public RuleSpecification(String id,
                           Map<Key, Constraint> constraints,
                           int priority,
                           Classification classification) {
    this.id = id;
    this.constraints = constraints;
    this.priority = priority;
    this.classification = classification;
  }

  public RuleSpecification() {
  }

  public String getId() {
    return id;
  }

  public Map<Key, Constraint> getConstraints() {
    return constraints;
  }

  public int getPriority() {
    return priority;
  }

  public Classification getClassification() {
    return classification;
  }
}
