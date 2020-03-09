package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.*;
import io.github.richardstartin.multimatcher.core.masks.MaskFactory;
import io.github.richardstartin.multimatcher.core.matchers.nodes.IntNode;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.ToIntFunction;

import static io.github.richardstartin.multimatcher.core.matchers.SelectivityHeuristics.avgCardinality;

public class IntMatcher<T, MaskType extends Mask<MaskType>> implements ConstraintAccumulator<T, MaskType>,
        Matcher<T, MaskType> {

  private final ToIntFunction<T> accessor;
  private final EnumMap<Operation, IntNode<MaskType>> children = new EnumMap<>(Operation.class);
  private final MaskType wildcards;
  private final MaskType empty;

  public IntMatcher(ToIntFunction<T> accessor, MaskFactory<MaskType> maskFactory, int max) {
    this.accessor = accessor;
    this.empty = maskFactory.emptySingleton();
    this.wildcards = maskFactory.contiguous(max);
  }

  @Override
  public MaskType match(T value, MaskType context) {
    MaskType result = match(accessor.applyAsInt(value));
    return context.inPlaceAnd(result);
  }

  @Override
  public boolean addConstraint(Constraint constraint, int priority) {
    Number number = constraint.getValue();
    int value = number.intValue();
    add(constraint.getOperation(), value, priority);
    wildcards.remove(priority);
    return true;
  }

  @Override
  public Matcher<T, MaskType> freeze() {
    optimise();
    wildcards.optimise();
    return this;
  }


  public float averageSelectivity() {
    return avgCardinality(children.values(), IntNode::averageSelectivity);
  }


  private void add(Operation relation, int threshold, int priority) {
    children.computeIfAbsent(relation, r -> new IntNode<>(r, empty)).add(threshold, priority);
  }

  private MaskType match(int value) {
    MaskType temp = empty.clone().inPlaceOr(wildcards);
    for (IntNode<MaskType> component : children.values()) {
      temp.inPlaceOr(component.apply(value, empty));
    }
    return temp;
  }

  private void optimise() {
    Map<Operation, IntNode<MaskType>> optimised = new EnumMap<>(Operation.class);
    children.forEach((op, node) -> optimised.put(op, node.optimise()));
    children.putAll(optimised);
  }


}
