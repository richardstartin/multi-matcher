package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.*;
import io.github.richardstartin.multimatcher.core.masks.MaskFactory;
import io.github.richardstartin.multimatcher.core.matchers.nodes.EqualityNode;
import io.github.richardstartin.multimatcher.core.matchers.nodes.InequalityNode;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.richardstartin.multimatcher.core.matchers.Utils.newArray;

public class GenericConstraintAccumulator<T, U, MaskType extends Mask<MaskType>>
        implements ConstraintAccumulator<T, MaskType> {

  protected final Function<T, U> accessor;
  protected final Supplier<Map<U, MaskType>> mapSupplier;
  protected final EnumMap<Operation, MutableNode<U, MaskType>> nodes = new EnumMap<>(Operation.class);
  protected final MaskType wildcard;
  protected final int max;
  protected final MaskFactory<MaskType> factory;

  public GenericConstraintAccumulator(Supplier<Map<U, MaskType>> mapSupplier,
                                      Function<T, U> accessor,
                                      MaskFactory<MaskType> factory,
                                      int max) {
    this.accessor = accessor;
    this.mapSupplier = mapSupplier;
    this.wildcard = factory.contiguous(max);
    this.factory = factory;
    this.max = max;
  }

  @Override
  public boolean addConstraint(Constraint constraint, int priority) {
    switch (constraint.getOperation()) {
      case NE:
        ((InequalityNode<U, MaskType>)nodes
                .computeIfAbsent(constraint.getOperation(),
                        op -> new InequalityNode<>(mapSupplier.get(), factory.contiguous(max))))
                .add(constraint.getValue(), priority);
        return true;
      case EQ:
        ((EqualityNode<U, MaskType>)nodes
                .computeIfAbsent(constraint.getOperation(),
                        op -> new EqualityNode<>(factory, mapSupplier.get())))
                .add(constraint.getValue(), priority);
        wildcard.remove(priority);
        return true;
      default:
        return false;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Matcher<T, MaskType> freeze() {
    wildcard.optimise();
    var frozen = (ClassificationNode<U, MaskType>[]) newArray(ClassificationNode.class, nodes.size());
    for (var node : nodes.values()) {
      node.link(nodes);
    }
    int i = 0;
    for (var node : nodes.values()) {
      frozen[i++] = node.freeze();
    }
    return new GenericMatcher<>(accessor, frozen, wildcard);
  }

}
