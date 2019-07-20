package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.*;
import uk.co.openkappa.bitrules.masks.MaskFactory;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static uk.co.openkappa.bitrules.Operation.EQ;
import static uk.co.openkappa.bitrules.Operation.NE;
import static uk.co.openkappa.bitrules.matchers.SelectivityHeuristics.avgCardinality;

public class GenericMatcher<T, U, MaskType extends Mask<MaskType>> implements MutableMatcher<T, MaskType> {

  private final Function<T, U> accessor;
  private final Supplier<Map<U, MaskType>> mapSupplier;
  private final EnumMap<Operation, MutableNode<U, MaskType>> nodes = new EnumMap<>(Operation.class);
  private final MaskType wildcard;
  private final MaskType empty;
  private final int max;

  public GenericMatcher(Supplier<Map<U, MaskType>> mapSupplier,
                        Function<T, U> accessor,
                        MaskFactory<MaskType> maskFactory,
                        int max) {
    this.accessor = accessor;
    this.mapSupplier = mapSupplier;
    this.wildcard = maskFactory.contiguous(max);
    this.empty = maskFactory.emptySingleton();
    this.max = max;
  }

  public MaskType match(T value, MaskType context) {
    MaskType mask = wildcard.and(context);
    U key = accessor.apply(value);
    MutableNode<U, MaskType> equality = nodes.get(EQ);
    if (null != equality) {
      mask = equality.match(key, context).inPlaceOr(mask);
    }
    MutableNode<U, MaskType> inequality = nodes.get(NE);
    if (null != inequality) {
      mask = mask.orNot(inequality.match(key, context), max);
    }
    return mask;
  }

  @Override
  public void addConstraint(Constraint constraint, int priority) {
    ((GenericEqualityNode<U, MaskType>)nodes
            .computeIfAbsent(constraint.getOperation(), op -> new GenericEqualityNode<>(mapSupplier.get(), empty, wildcard)))
    .add(constraint.getValue(), priority);
    wildcard.remove(priority);
  }

  @Override
  public Matcher<T, MaskType> freeze() {
    wildcard.optimise();
    EnumMap<Operation, ClassificationNode<U, MaskType>> optimised = new EnumMap<>(Operation.class);
    nodes.forEach((op, node) -> optimised.put(op, node.optimise()));
    return new OptimisedGenericMatcher<>(accessor, optimised, wildcard, max);
  }

  @Override
  public float averageSelectivity() {
    return avgCardinality(nodes.values(), ClassificationNode::averageSelectivity);
  }

  private static class OptimisedGenericMatcher<T, U, MaskType extends Mask<MaskType>> implements Matcher<T, MaskType> {

    private final Function<T, U> accessor;
    private final EnumMap<Operation, ClassificationNode<U, MaskType>> nodes;
    private final MaskType wildcard;
    private final int max;

    private OptimisedGenericMatcher(Function<T, U> accessor,
                                    EnumMap<Operation, ClassificationNode<U, MaskType>> nodes,
                                    MaskType wildcard, int max) {
      this.accessor = accessor;
      this.nodes = nodes;
      this.wildcard = wildcard;
      this.max = max;
    }

    @Override
    public MaskType match(T value, MaskType context) {
      MaskType mask = wildcard.and(context);
      U key = accessor.apply(value);
      ClassificationNode<U, MaskType> equality = nodes.get(EQ);
      if (null != equality) {
        mask = equality.match(key, context).inPlaceOr(mask);
      }
      ClassificationNode<U, MaskType> inequality = nodes.get(NE);
      if (null != inequality) {
        mask = mask.orNot(inequality.match(key, context), max);
      }
      return mask;
    }

    @Override
    public float averageSelectivity() {
      return avgCardinality(nodes.values(), ClassificationNode::averageSelectivity);
    }
  }
}
