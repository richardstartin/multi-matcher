package uk.co.openkappa.bitrules;

import java.util.*;

import static java.util.Objects.requireNonNull;


public class MatchingConstraint<Key, Classification> {

  public static <K, C> Builder<K, C> named(String ruleId) {
    return new Builder<>(ruleId);
  }


  public static <K, C> Builder<K, C> anonymous() {
    return new Builder<>(UUID.randomUUID().toString());
  }

  public static class Builder<K, C> {

    private final String id;
    private Map<K, Constraint> constraints = new HashMap<>();
    private int priority;
    private C classification;

    private Builder(String id) {
      this.id = id;
    }

    public Builder<K, C> eq(K key, Object value) {
      return constraint(key, Constraint.equalTo(value));
    }

    public Builder<K, C> neq(K key, Object value) {
      return constraint(key, Constraint.notEqualTo(value));
    }

    public Builder<K, C> lt(K key, Comparable<?> value) {
      return constraint(key, Constraint.lessThan(value));
    }

    public Builder<K, C> le(K key, Comparable<?> value) {
      return constraint(key, Constraint.lessThanOrEqualTo(value));
    }

    public Builder<K, C> gt(K key, Comparable<?> value) {
      return constraint(key, Constraint.greaterThan(value));
    }

    public Builder<K, C> ge(K key, Comparable<?> value) {
      return constraint(key, Constraint.greaterThanOrEqualTo(value));
    }

    public Builder<K, C> startsWith(K key, String prefix) {
      return constraint(key, Constraint.startsWith(prefix));
    }

    public Builder<K, C> priority(int value) {
      this.priority = value;
      return this;
    }

    public Builder<K, C> constraint(K key, Constraint constraint) {
      this.constraints.put(requireNonNull(key), requireNonNull(constraint));
      return this;
    }

    public Builder<K, C> classification(C classification) {
      this.classification = requireNonNull(classification);
      return this;
    }


    public MatchingConstraint<K, C> build() {
      if (constraints.isEmpty()) {
        throw new IllegalStateException("Unconstrained rule");
      }
      return new MatchingConstraint<>(id, constraints, priority, requireNonNull(classification));
    }
  }

  private String id;
  private Map<Key, Constraint> constraints;
  private int priority;
  private Classification classification;

  public MatchingConstraint(String id,
                            Map<Key, Constraint> constraints,
                            int priority,
                            Classification classification) {
    this.id = id;
    this.constraints = constraints;
    this.priority = priority;
    this.classification = classification;
  }

  public MatchingConstraint() {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MatchingConstraint<?, ?> that = (MatchingConstraint<?, ?>) o;
    return priority == that.priority &&
            Objects.equals(id, that.id) &&
            Objects.equals(constraints, that.constraints) &&
            Objects.equals(classification, that.classification);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, constraints, priority, classification);
  }

  @Override
  public String toString() {
    return "MatchingConstraint{" +
            "id='" + id + '\'' +
            ", constraints=" + constraints +
            ", priority=" + priority +
            ", classification=" + classification +
            '}';
  }
}
