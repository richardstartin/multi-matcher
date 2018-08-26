package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Operation;

import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class ComparableNode<T, MaskType extends Mask<MaskType>> {

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
