package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.*;
import uk.co.openkappa.bitrules.masks.MaskFactory;
import uk.co.openkappa.bitrules.matchers.nodes.IntNode;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.ToIntFunction;

import static uk.co.openkappa.bitrules.matchers.SelectivityHeuristics.avgCardinality;

public class IntMatcher<T, MaskType extends Mask<MaskType>> implements MutableMatcher<T, MaskType> {

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
    MaskType result = match(accessor.applyAsInt(value), context);
    return context.inPlaceAnd(result.inPlaceOr(wildcards));
  }

  @Override
  public void addConstraint(Constraint constraint, int priority) {
    Number number = constraint.getValue();
    int value = number.intValue();
    add(constraint.getOperation(), value, priority);
    wildcards.remove(priority);
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

  private MaskType match(int value, MaskType result) {
    MaskType temp = empty.clone();
    for (IntNode<MaskType> component : children.values()) {
      temp = temp.inPlaceOr(component.apply(value, result.clone()));
    }
    return result.inPlaceAnd(temp);
  }

  private void optimise() {
    Map<Operation, IntNode<MaskType>> optimised = new EnumMap<>(Operation.class);
    children.forEach((op, node) -> optimised.put(op, node.optimise()));
    children.putAll(optimised);
  }


}
