package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.*;
import io.github.richardstartin.multimatcher.core.masks.MaskFactory;
import io.github.richardstartin.multimatcher.core.matchers.nodes.LongNode;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.ToLongFunction;

import static io.github.richardstartin.multimatcher.core.matchers.SelectivityHeuristics.avgCardinality;

public class LongMatcher<T, MaskType extends Mask<MaskType>> implements ConstraintAccumulator<T, MaskType>,
        Matcher<T, MaskType> {

  private final ToLongFunction<T> accessor;
  private final Map<Operation, LongNode<MaskType>> children = new EnumMap<>(Operation.class);
  private final MaskType empty;
  private final MaskType wildcards;

  public LongMatcher(ToLongFunction<T> accessor, MaskFactory<MaskType> maskFactory, int max) {
    this.accessor = accessor;
    this.empty = maskFactory.emptySingleton();
    this.wildcards = maskFactory.contiguous(max);
  }

  @Override
  public MaskType match(T value, MaskType context) {
    MaskType result = match(accessor.applyAsLong(value), context);
    return context.inPlaceAnd(result.inPlaceOr(wildcards));
  }

  @Override
  public boolean addConstraint(Constraint constraint, int priority) {
    Number number = constraint.getValue();
    long value = number.longValue();
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


  private void add(Operation relation, long threshold, int priority) {
    children.computeIfAbsent(relation, r -> new LongNode<>(r, empty)).add(threshold, priority);
  }

  private MaskType match(long value, MaskType result) {
    MaskType temp = empty.clone();
    for (LongNode<MaskType> component : children.values()) {
      temp = temp.inPlaceOr(component.apply(value, result.clone()));
    }
    return result.inPlaceAnd(temp);
  }

  private void optimise() {
    Map<Operation, LongNode<MaskType>> optimised = new EnumMap<>(Operation.class);
    children.forEach((op, node) -> optimised.put(op, node.optimise()));
    children.putAll(optimised);
  }

  @Override
  public float averageSelectivity() {
    return avgCardinality(children.values(), LongNode::averageSelectivity);
  }

}
