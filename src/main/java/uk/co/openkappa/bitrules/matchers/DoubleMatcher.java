package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.Constraint;
import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Matcher;

import java.util.function.ToDoubleFunction;

public class DoubleMatcher<T, MaskType extends Mask<MaskType>> implements Matcher<T, MaskType> {

  private final ToDoubleFunction<T> accessor;
  private final CompositeDoubleNode<MaskType> node;
  private final MaskType wildcards;

  public DoubleMatcher(ToDoubleFunction<T> accessor, Class<MaskType> type, int max) {
    this.accessor = accessor;
    this.node = new CompositeDoubleNode(type);
    this.wildcards = Masks.createFull(type, max);
  }

  @Override
  public MaskType match(T value, MaskType context) {
    MaskType result = node.match(accessor.applyAsDouble(value), context);
    return context.inPlaceAnd(result.or(wildcards));
  }

  @Override
  public void addConstraint(Constraint constraint, int priority) {
    Number number = coerceValue(constraint);
    double value = number.doubleValue();
    node.add(constraint.getOperation(), value, priority);
    wildcards.remove(priority);
  }

  @Override
  public void freeze() {
    node.optimise();
    wildcards.optimise();
  }
}
