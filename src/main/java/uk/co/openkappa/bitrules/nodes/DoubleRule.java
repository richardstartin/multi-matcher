package uk.co.openkappa.bitrules.nodes;

import uk.co.openkappa.bitrules.Constraint;
import uk.co.openkappa.bitrules.Rule;
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
    Container result = node.match(accessor.applyAsDouble(value), context);
    return context.iand(result.or(wildcards));
  }

  @Override
  public void addConstraint(Constraint constraint, short priority) {
    Number number = coerceValue(constraint);
    double value = number.doubleValue();
    node.add(constraint.getOperation(), value, priority);
    wildcards = wildcards.remove(priority);
  }

  @Override
  public void freeze() {
    node.optimise();
    wildcards = wildcards.runOptimize();
  }
}
