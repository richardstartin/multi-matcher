package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.Constraint;
import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Matcher;
import uk.co.openkappa.bitrules.Operation;

import java.util.*;
import java.util.function.Function;

import static uk.co.openkappa.bitrules.matchers.Masks.singleton;

public class ComparableMatcher<T, U, MaskType extends Mask<MaskType>> implements Matcher<T, MaskType> {

  private final Function<T, U> accessor;
  private final MaskType wildcards;
  private final CompositeComparableNode<U, MaskType> node;

  public ComparableMatcher(Function<T, U> accessor, Comparator<U> comparator, Class<MaskType> type, int max) {
    this.accessor = accessor;
    this.node = new CompositeComparableNode<>(comparator, type);
    this.wildcards = Masks.wildcards(type, max);
  }

  @Override
  public MaskType match(T value, MaskType context) {
    MaskType result = node.match(accessor.apply(value), context);
    return context.inPlaceAnd(result.or(wildcards));
  }

  @Override
  public void addConstraint(Constraint constraint, int priority) {
    node.add(constraint.getOperation(), (U)constraint.getValue(), priority);
    wildcards.remove(priority);
  }

  @Override
  public void freeze() {
    node.optimise();
    wildcards.optimise();
  }

  @Override
  public String toString() {
    return node + ", *: " + wildcards;
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
              sets.entrySet().stream().map(Map.Entry::getKey).iterator(),
              sets.entrySet().stream().map(Map.Entry::getValue).iterator());
    }
  }

  private static class CompositeComparableNode<T, MaskType extends Mask<MaskType>> {

    private final Comparator<T> comparator;
    private final Map<Operation, ComparableNode<T, MaskType>> children = new EnumMap<>(Operation.class);
    private final MaskType empty;

    public CompositeComparableNode(Comparator<T> comparator, Class<MaskType> type) {
      this.comparator = comparator;
      this.empty = singleton(type);
    }

    public void add(Operation relation, T threshold, int priority) {
      children.computeIfAbsent(relation, r -> new ComparableNode<>(comparator, r, empty)).add(threshold, priority);
    }

    public MaskType match(T value, MaskType result) {
      MaskType temp = empty.clone();
      for (ComparableNode<T, MaskType> component : children.values()) {
        temp = temp.inPlaceOr(component.match(value, result.clone()));
      }
      return result.and(temp);
    }

    public void optimise() {
      children.values().forEach(ComparableNode::optimise);
    }

    @Override
    public String toString() {
      return children.toString();
    }
  }
}
