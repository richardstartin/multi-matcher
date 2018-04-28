package com.openkappa.bitrules.nodes;

import com.openkappa.bitrules.Constraint;
import com.openkappa.bitrules.Rule;
import org.roaringbitmap.Container;
import org.roaringbitmap.RunContainer;

import java.util.function.ToLongFunction;

public class LongRule<T> implements Rule<T> {

  private final ToLongFunction<T> accessor;
  private final CompositeLongNode node;
  private Container wildcards = RunContainer.full();

  public LongRule(ToLongFunction<T> accessor) {
    this.accessor = accessor;
    this.node = new CompositeLongNode();
  }

  @Override
  public Container match(T value, Container context) {
    Container result = node.match(accessor.applyAsLong(value), context);
    return context.iand(result.or(wildcards));
  }

  @Override
  public void addConstraint(Constraint constraint, short priority) {
    Number number = coerceValue(constraint);
    long value = number.longValue();
    node.add(constraint.getOperation(), value, priority);
    wildcards = wildcards.remove(priority);
  }

  @Override
  public void freeze() {
    node.optimise();
  }
}
