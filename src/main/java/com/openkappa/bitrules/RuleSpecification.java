package com.openkappa.bitrules;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;


public class RuleSpecification {

  public static RuleSpecification of(String id,
                                     String description,
                                     Map<String, Constraint> constraints,
                                     short priority,
                                     String classification) {
    return new RuleSpecification(id, description, constraints, priority, classification);
  }

  @JsonProperty("id")
  private String id;
  @JsonProperty("description")
  private String description;
  @JsonProperty("constraints")
  private Map<String, Constraint> constraints;
  @JsonProperty("priority")
  private short priority;
  @JsonProperty("classification")
  private String classification;

  public RuleSpecification(String id,
                           String description,
                           Map<String, Constraint> constraints,
                           short priority,
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

  public short getPriority() {
    return priority;
  }

  public String getClassification() {
    return classification;
  }
}
