package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.Constraint;
import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Matcher;
import uk.co.openkappa.bitrules.MutableMatcher;
import uk.co.openkappa.bitrules.masks.MaskFactory;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class GenericMatcher<T, U, MaskType extends Mask<MaskType>> implements MutableMatcher<T, MaskType> {

  private final Function<T, U> accessor;
  private final GenericEqualityNode<U, MaskType> node;
  private final MaskType wildcard;

  public GenericMatcher(Supplier<Map<U, MaskType>> mapSupplier, Function<T, U> accessor, MaskFactory<MaskType> maskFactory, int max) {
    this.accessor = accessor;
    this.wildcard = maskFactory.contiguous(max);
    this.node = new GenericEqualityNode<>(mapSupplier.get(), maskFactory.emptySingleton(), wildcard);
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
  public Matcher<T, MaskType> freeze() {
    wildcard.optimise();
    return new OptimisedGenericMatcher<>(accessor, node.optimise(), wildcard);
  }

  @Override
  public float averageSelectivity() {
    return node.averageSelectivity();
  }

  private static class OptimisedGenericMatcher<T, U, MaskType extends Mask<MaskType>> implements Matcher<T, MaskType> {

    private final Function<T, U> accessor;
    private final ClassificationNode<U, MaskType> node;
    private final MaskType wildcard;

    private OptimisedGenericMatcher(Function<T, U> accessor, ClassificationNode<U, MaskType> node, MaskType wildcard) {
      this.accessor = accessor;
      this.node = node;
      this.wildcard = wildcard;
    }

    @Override
    public MaskType match(T value, MaskType context) {
      MaskType mask = wildcard.and(context);
      return node.match(accessor.apply(value), context).inPlaceOr(mask);
    }

    @Override
    public float averageSelectivity() {
      return node.averageSelectivity();
    }
  }
}
