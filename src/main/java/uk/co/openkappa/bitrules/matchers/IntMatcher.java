package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.Constraint;
import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Matcher;

import java.util.function.ToIntFunction;

public class IntMatcher<T, MaskType extends Mask<MaskType>> implements Matcher<T, MaskType> {

  private final ToIntFunction<T> accessor;
  private final CompositeIntNode<MaskType> node;
  private final MaskType wildcards;

  public IntMatcher(ToIntFunction<T> accessor, Class<MaskType> type, int max) {
    this.accessor = accessor;
    this.node = new CompositeIntNode<>(Masks.singleton(type));
    this.wildcards = Masks.wildcards(type, max);
  }

  @Override
  public MaskType match(T value, MaskType context) {
    MaskType result = node.match(accessor.applyAsInt(value), context);
    return context.inPlaceAnd(result.or(wildcards));
  }

  @Override
  public void addConstraint(Constraint constraint, int priority) {
    Number number = coerceValue(constraint);
    int value = number.intValue();
    node.add(constraint.getOperation(), value, priority);
    wildcards.remove(priority);
  }

  @Override
  public void freeze() {
    node.optimise();
    wildcards.optimise();
  }
}
