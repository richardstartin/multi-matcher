package uk.co.openkappa.bitrules.nodes;

import uk.co.openkappa.bitrules.Constraint;
import uk.co.openkappa.bitrules.Rule;
import org.roaringbitmap.Container;
import org.roaringbitmap.RunContainer;

import java.util.Comparator;
import java.util.function.Function;

public class ComparableRule<T, U> implements Rule<T> {



  private final Function<T, U> accessor;
  private Container wildcards = RunContainer.full();
  private final CompositeComparableNode<U> node;

  public ComparableRule(Function<T, U> accessor, Comparator<U> comparator) {
    this.accessor = accessor;
    this.node = new CompositeComparableNode<>(comparator);
  }

  @Override
  public Container match(T value, Container context) {
    Container result = node.match(accessor.apply(value), context);
    return context.iand(result.or(wildcards));
  }

  @Override
  public void addConstraint(Constraint constraint, short priority) {
    node.add(constraint.getOperation(), (U)constraint.getValue(), priority);
    wildcards = wildcards.remove(priority);
  }
}
