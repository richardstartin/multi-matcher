package uk.co.openkappa.bitrules;

import java.util.Map;


public class RuleSpecification {

  public static RuleSpecification of(String id,
                                     String description,
                                     Map<String, Constraint> constraints,
                                     int priority,
                                     String classification) {
    return new RuleSpecification(id, description, constraints, priority, classification);
  }

  private String id;
  private String description;
  private Map<String, Constraint> constraints;
  private int priority;
  private String classification;

  public RuleSpecification(String id,
                           String description,
                           Map<String, Constraint> constraints,
                           int priority,
                           String classification) {
    this.id = id;
    this.description = description;
    this.constraints = constraints;
    this.priority = priority;
    this.classification = classification;
  }

  public RuleSpecification() {
  }

  public String getId() {
    return id;
  }

  public String getDescription() {
    return description;
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
