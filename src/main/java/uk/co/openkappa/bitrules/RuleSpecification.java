package uk.co.openkappa.bitrules;

import java.util.Map;


public class RuleSpecification {

  public static RuleSpecification of(String id,
                                     Map<String, Constraint> constraints,
                                     int priority,
                                     String classification) {
    return new RuleSpecification(id, constraints, priority, classification);
  }

  private String id;
  private Map<String, Constraint> constraints;
  private int priority;
  private String classification;

  public RuleSpecification(String id,
                           Map<String, Constraint> constraints,
                           int priority,
                           String classification) {
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

  public String getClassification() {
    return classification;
  }
}
