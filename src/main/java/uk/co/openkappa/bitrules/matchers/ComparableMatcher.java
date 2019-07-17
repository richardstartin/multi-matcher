package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.*;
import uk.co.openkappa.bitrules.masks.MaskFactory;

import java.util.*;
import java.util.function.Function;

public class ComparableMatcher<T, U, MaskType extends Mask<MaskType>> implements MutableMatcher<T, MaskType> {

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
  public void addConstraint(Constraint constraint, int priority) {
    add(constraint.getOperation(), constraint.getValue(), priority);
    wildcards.remove(priority);
  }

  @Override
  public Matcher<T, MaskType> freeze() {
    optimise();
    wildcards.optimise();
    return this;
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

  public static class ComparableNode<T, MaskType extends Mask<MaskType>> {

    private final MaskType empty;
    private final NavigableMap<T, MaskType> sets;
    private final Operation operation;

    public ComparableNode(Comparator<T> comparator, Operation operation, MaskType empty) {
      this.sets = new TreeMap<>(comparator);
      this.operation = operation;
      this.empty = empty;
    }

    public void add(T value, int priority) {
      sets.compute(value, (k, v) -> {
        if (v == null) {
          v = empty.clone();
        }
        v.add(priority);
        return v;
      });
    }

    public MaskType match(T value, MaskType context) {
      switch (operation) {
        case GE:
        case EQ:
        case LE:
          return context.inPlaceAnd(sets.getOrDefault(value, empty));
        case LT:
          Map.Entry<T, MaskType> higher = sets.higherEntry(value);
          return context.inPlaceAnd(null == higher ? empty : higher.getValue());
        case GT:
          Map.Entry<T, MaskType> lower = sets.lowerEntry(value);
          return context.inPlaceAnd(null == lower ? empty : lower.getValue());
        default:
          return context;
      }
    }

    public ComparableNode<T, MaskType> optimise() {
      switch (operation) {
        case GE:
        case GT:
          rangeEncode();
          return this;
        case LE:
        case LT:
          reverseRangeEncode();
          return this;
        default:
          return this;
      }
    }

    private void rangeEncode() {
      MaskType prev = null;
      for (Map.Entry<T, MaskType> set : sets.entrySet()) {
        if (prev != null) {
          sets.put(set.getKey(), set.getValue().inPlaceOr(prev));
        }
        prev = set.getValue();
      }
    }

    private void reverseRangeEncode() {
      MaskType prev = null;
      for (Map.Entry<T, MaskType> set : sets.descendingMap().entrySet()) {
        if (prev != null) {
          sets.put(set.getKey(), set.getValue().inPlaceOr(prev));
        }
        prev = set.getValue();
      }
    }

    @Override
    public String toString() {
      return Nodes.toString(sets.size(), operation,
              sets.keySet().stream().iterator(),
              sets.values().stream().iterator());
    }
  }

}
