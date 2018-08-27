package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.Constraint;
import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Matcher;

import java.util.Comparator;
import java.util.function.Function;

public class ComparableMatcher<T, U, MaskType extends Mask<MaskType>> implements Matcher<T, MaskType> {

  private final Function<T, U> accessor;
  private final MaskType wildcards;
  private final CompositeComparableNode<U, MaskType> node;

  public ComparableMatcher(Function<T, U> accessor, Comparator<U> comparator, Class<MaskType> type, int max) {
    this.accessor = accessor;
    this.node = new CompositeComparableNode<>(comparator, type);
    this.wildcards = Masks.createFull(type, max);
  }

  @Override
  public MaskType match(T value, MaskType context) {
    MaskType result = node.match(accessor.apply(value), context);
    return context.inPlaceAnd(result.or(wildcards));
  }

  @Override
  public void addConstraint(Constraint constraint, int priority) {
    node.add(constraint.getOperation(), (U)constraint.getValue(), priority);
    wildcards.remove(priority);
  }

  @Override
  public void freeze() {
    node.optimise();
    wildcards.optimise();
  }

  @Override
  public String toString() {
    return node + ", *: " + wildcards;
  }
}
