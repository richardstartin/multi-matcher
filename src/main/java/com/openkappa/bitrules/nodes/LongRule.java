package com.openkappa.bitrules.nodes;

import com.openkappa.bitrules.Constraint;
import com.openkappa.bitrules.IntRelation;
import com.openkappa.bitrules.LongRelation;
import com.openkappa.bitrules.Rule;
import org.roaringbitmap.Container;
import org.roaringbitmap.RunContainer;

import java.util.function.ToIntFunction;
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
    Container result = node.apply(accessor.applyAsLong(value), context);
    return context.iand(result.or(wildcards));
  }

  @Override
  public void addConstraint(Constraint constraint, short priority) {
    LongRelation relation = LongRelation.from(constraint.getOperation());
    long value = ((Number) constraint.getValue()).longValue();
    node.add(relation, value, priority);
    wildcards = wildcards.remove(priority);
  }
}
