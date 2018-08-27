package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.Constraint;
import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Matcher;

import java.util.function.Function;

public class GenericMatcher<T, U, MaskType extends Mask<MaskType>> implements Matcher<T, MaskType> {

  private final Function<T, U> accessor;
  private final GenericEqualityNode<U, MaskType> node;
  private final MaskType wildcard;

  public GenericMatcher(Function<T, U> accessor, Class<MaskType> type, int max) {
    this.accessor = accessor;
    this.wildcard = Masks.wildcards(type, max);
    this.node = new GenericEqualityNode<>(Masks.singleton(type));
  }

  public MaskType match(T value, MaskType context) {
    return node.match(accessor.apply(value), context).inPlaceOr(wildcard);
  }

  @Override
  public void addConstraint(Constraint constraint, int priority) {
    node.add(constraint.getValue(), priority);
    wildcard.remove(priority);
  }

  @Override
  public void freeze() {
    node.optimise();
    wildcard.optimise();
  }
}
