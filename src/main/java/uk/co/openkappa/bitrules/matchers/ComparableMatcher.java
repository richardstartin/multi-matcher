package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.*;
import uk.co.openkappa.bitrules.masks.MaskFactory;
import uk.co.openkappa.bitrules.matchers.nodes.ComparableNode;

import java.util.*;
import java.util.function.Function;

import static uk.co.openkappa.bitrules.matchers.SelectivityHeuristics.avgCardinality;

public class ComparableMatcher<T, U, MaskType extends Mask<MaskType>> implements ConstraintAccumulator<T, MaskType>,
        Matcher<T, MaskType> {

  private final Function<T, U> accessor;
  private final MaskType wildcards;
  private final Comparator<U> comparator;
  private final EnumMap<Operation, ComparableNode<U, MaskType>> children = new EnumMap<>(Operation.class);
  private final MaskType empty;

  public ComparableMatcher(Function<T, U> accessor, Comparator<U> comparator, MaskFactory<MaskType> maskFactory, int max) {
    this.accessor = accessor;
    this.comparator = comparator;
    this.empty = maskFactory.emptySingleton();
    this.wildcards = maskFactory.contiguous(max);
  }

  @Override
  public MaskType match(T value, MaskType context) {
    MaskType result = matchValue(accessor.apply(value), context);
    return context.inPlaceAnd(result.or(wildcards));
  }

  @Override
  public boolean addConstraint(Constraint constraint, int priority) {
    add(constraint.getOperation(), constraint.getValue(), priority);
    wildcards.remove(priority);
    return true;
  }

  @Override
  public Matcher<T, MaskType> freeze() {
    optimise();
    wildcards.optimise();
    return this;
  }

  @Override
  public float averageSelectivity() {
    return avgCardinality(children.values(), ComparableNode::averageSelectivity);
  }

  @Override
  public String toString() {
    return children + ", *: " + wildcards;
  }

  private void add(Operation relation, U threshold, int priority) {
    children.computeIfAbsent(relation, r -> new ComparableNode<>(comparator, r, empty)).add(threshold, priority);
  }

  private MaskType matchValue(U value, MaskType context) {
    MaskType temp = empty.clone();
    for (ComparableNode<U, MaskType> component : children.values()) {
      temp = temp.inPlaceOr(component.match(value, context.clone()));
    }
    return context.and(temp);
  }

  public void optimise() {
    Map<Operation, ComparableNode<U, MaskType>> optimised = new EnumMap<>(Operation.class);
    children.forEach((op, node) -> optimised.put(op, node.optimise()));
    children.putAll(optimised);
  }

}
