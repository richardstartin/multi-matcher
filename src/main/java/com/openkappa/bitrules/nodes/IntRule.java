package com.openkappa.bitrules.nodes;

import com.openkappa.bitrules.Constraint;
import com.openkappa.bitrules.IntRelation;
import com.openkappa.bitrules.Rule;
import org.roaringbitmap.Container;
import org.roaringbitmap.RunContainer;

import java.util.function.ToIntFunction;

public class IntRule<T> implements Rule<T> {

  private final ToIntFunction<T> accessor;
  private final CompositeIntNode node;
  private Container wildcards = RunContainer.full();

  public IntRule(ToIntFunction<T> accessor) {
    this.accessor = accessor;
    this.node = new CompositeIntNode();
  }

  @Override
  public Container match(T value, Container context) {
    Container result = node.apply(accessor.applyAsInt(value), context);
    return context.iand(result.or(wildcards));
  }

  @Override
  public void addConstraint(Constraint constraint, short priority) {
    IntRelation relation = IntRelation.from(constraint.getOperation());
    int value = ((Number) constraint.getValue()).intValue();
    node.add(relation, value, priority);
    wildcards = wildcards.remove(priority);
  }

  @Override
  public void freeze() {
    node.optimise();
  }
}
