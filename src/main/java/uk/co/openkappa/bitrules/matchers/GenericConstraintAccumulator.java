package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.*;
import uk.co.openkappa.bitrules.masks.MaskFactory;
import uk.co.openkappa.bitrules.matchers.nodes.GenericEqualityNode;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static uk.co.openkappa.bitrules.Operation.EQ;
import static uk.co.openkappa.bitrules.Operation.NE;
import static uk.co.openkappa.bitrules.matchers.SelectivityHeuristics.avgCardinality;

public class GenericConstraintAccumulator<T, U, MaskType extends Mask<MaskType>> implements ConstraintAccumulator<T, MaskType> {

  protected final Function<T, U> accessor;
  protected final Supplier<Map<U, MaskType>> mapSupplier;
  protected final EnumMap<Operation, MutableNode<U, MaskType>> nodes = new EnumMap<>(Operation.class);
  protected final MaskType wildcard;
  protected final MaskType empty;
  protected final int max;

  public GenericConstraintAccumulator(Supplier<Map<U, MaskType>> mapSupplier,
                                      Function<T, U> accessor,
                                      MaskFactory<MaskType> maskFactory,
                                      int max) {
    this.accessor = accessor;
    this.mapSupplier = mapSupplier;
    this.wildcard = maskFactory.contiguous(max);
    this.empty = maskFactory.emptySingleton();
    this.max = max;
  }

  @Override
  public boolean addConstraint(Constraint constraint, int priority) {
    switch (constraint.getOperation()) {
      case NE:
      case EQ:
        ((GenericEqualityNode<U, MaskType>)nodes
                .computeIfAbsent(constraint.getOperation(), op -> new GenericEqualityNode<>(mapSupplier.get(), empty, wildcard)))
                .add(constraint.getValue(), priority);
        wildcard.remove(priority);
        return true;
      default:
        return false;
    }
  }

  @Override
  public Matcher<T, MaskType> freeze() {
    wildcard.optimise();
    EnumMap<Operation, ClassificationNode<U, MaskType>> optimised = new EnumMap<>(Operation.class);
    nodes.forEach((op, node) -> optimised.put(op, node.optimise()));
    return new OptimisedGenericMatcher<>(accessor, optimised, wildcard, empty, max);
  }

  private static class OptimisedGenericMatcher<T, U, MaskType extends Mask<MaskType>> implements Matcher<T, MaskType> {

    private final Function<T, U> accessor;
    private final EnumMap<Operation, ClassificationNode<U, MaskType>> nodes;
    private final MaskType wildcard;
    private final MaskType empty;
    private final int max;

    private OptimisedGenericMatcher(Function<T, U> accessor,
                                    EnumMap<Operation, ClassificationNode<U, MaskType>> nodes,
                                    MaskType wildcard, MaskType empty, int max) {
      this.accessor = accessor;
      this.nodes = nodes;
      this.wildcard = wildcard;
      this.empty = empty;
      this.max = max;
    }

    @Override
    public MaskType match(T input, MaskType context) {
      U value = accessor.apply(input);
      MaskType result = empty.clone();
      for (var component : nodes.entrySet()) {
        var op = component.getKey();
        var node = component.getValue();
        if (op == NE) {
          result = result.orNot(node.match(value), max);
        } else {
          result = result.inPlaceOr(node.match(value));
        }
      }
      return context.inPlaceAnd(result.inPlaceOr(wildcard));
    }

    @Override
    public float averageSelectivity() {
      return avgCardinality(nodes.values(), ClassificationNode::averageSelectivity);
    }
  }
}
