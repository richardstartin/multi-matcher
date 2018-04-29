package uk.co.openkappa.bitrules;

import java.util.Map;


public class RuleSpecification<C> {

  public static <C> RuleSpecification of(String id,
                                     Map<String, Constraint> constraints,
                                     int priority,
                                     C classification) {
    return new RuleSpecification(id, constraints, priority, classification);
  }

  private String id;
  private Map<String, Constraint> constraints;
  private int priority;
  private C classification;

  public RuleSpecification(String id,
                           Map<String, Constraint> constraints,
                           int priority,
                           C classification) {
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

  public Map<String, Constraint> getConstraints() {
    return constraints;
  }

  public int getPriority() {
    return priority;
  }

  public C getClassification() {
    return classification;
  }
}
