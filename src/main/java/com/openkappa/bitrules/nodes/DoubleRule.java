package com.openkappa.bitrules.nodes;

import com.openkappa.bitrules.Constraint;
import com.openkappa.bitrules.DoubleRelation;
import com.openkappa.bitrules.Rule;
import org.roaringbitmap.Container;
import org.roaringbitmap.RunContainer;

import java.util.function.ToDoubleFunction;

public class DoubleRule<T> implements Rule<T> {

  private final ToDoubleFunction<T> accessor;
  private final CompositeDoubleNode node;
  private Container wildcards = RunContainer.full();

  public DoubleRule(ToDoubleFunction<T> accessor) {
    this.accessor = accessor;
    this.node = new CompositeDoubleNode();
  }

  @Override
  public Container match(T value, Container context) {
    Container result = node.apply(accessor.applyAsDouble(value), context);
    return context.iand(result.or(wildcards));
  }

  @Override
  public void addConstraint(Constraint constraint, short priority) {
    DoubleRelation relation = DoubleRelation.from(constraint.getOperation());
    Number number = coerceValue(constraint);
    double value = number.doubleValue();
    node.add(relation, value, priority);
    wildcards = wildcards.remove(priority);
  }

  @Override
  public void freeze() {
    node.optimise();
  }
}
