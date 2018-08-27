package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.Constraint;
import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Matcher;

import java.util.function.ToLongFunction;

public class LongMatcher<T, MaskType extends Mask<MaskType>> implements Matcher<T, MaskType> {

  private final ToLongFunction<T> accessor;
  private final CompositeLongNode<MaskType> node;
  private final MaskType wildcards;

  public LongMatcher(ToLongFunction<T> accessor, Class<MaskType> type, int max) {
    this.accessor = accessor;
    this.node = new CompositeLongNode<>(Masks.singleton(type));
    this.wildcards = Masks.createFull(type, max);
  }

  @Override
  public MaskType match(T value, MaskType context) {
    MaskType result = node.match(accessor.applyAsLong(value), context);
    return context.inPlaceAnd(result.or(wildcards));
  }

  @Override
  public void addConstraint(Constraint constraint, int priority) {
    Number number = coerceValue(constraint);
    long value = number.longValue();
    node.add(constraint.getOperation(), value, priority);
    wildcards.remove(priority);
  }

  @Override
  public void freeze() {
    node.optimise();
    wildcards.optimise();
  }
}
