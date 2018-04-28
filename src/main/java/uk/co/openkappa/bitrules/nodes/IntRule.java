package uk.co.openkappa.bitrules.nodes;

import uk.co.openkappa.bitrules.Constraint;
import uk.co.openkappa.bitrules.Rule;
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
    Container result = node.match(accessor.applyAsInt(value), context);
    return context.iand(result.or(wildcards));
  }

  @Override
  public void addConstraint(Constraint constraint, short priority) {
    Number number = coerceValue(constraint);
    int value = number.intValue();
    node.add(constraint.getOperation(), value, priority);
    wildcards = wildcards.remove(priority);
  }

  @Override
  public void freeze() {
    node.optimise();
  }
}
