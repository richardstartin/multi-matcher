package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.Constraint;
import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Matcher;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class GenericMatcher<T, U, MaskType extends Mask<MaskType>> implements Matcher<T, MaskType> {

  private final Function<T, U> accessor;
  private final GenericEqualityNode<U, MaskType> node;
  private final MaskType wildcard;

  public GenericMatcher(Supplier<Map<U, MaskType>> mapSupplier, Function<T, U> accessor, Class<MaskType> type, int max) {
    this.accessor = accessor;
    this.wildcard = Masks.wildcards(type, max);
    this.node = new GenericEqualityNode<>(mapSupplier.get(), Masks.singleton(type), wildcard);
  }

  public MaskType match(T value, MaskType context) {
    MaskType mask = wildcard.and(context);
    return node.match(accessor.apply(value), context).inPlaceOr(mask);
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
