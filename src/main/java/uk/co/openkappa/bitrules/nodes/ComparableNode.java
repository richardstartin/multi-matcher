package uk.co.openkappa.bitrules.nodes;

import uk.co.openkappa.bitrules.Operation;
import org.roaringbitmap.ArrayContainer;
import org.roaringbitmap.Container;

import java.util.*;

public class ComparableNode<T> {

  private static final Container EMPTY = new ArrayContainer();

  private final NavigableMap<T, Container> sets;
  private final Operation operation;

  public ComparableNode(Comparator<T> comparator, Operation operation) {
    this.sets = new TreeMap<>(comparator);
    this.operation = operation;
  }

  public void add(T value, short priority) {
    sets.compute(value, (k, v) -> v == null ? EMPTY.clone().add(priority) : v.add(priority));
  }

  public Container match(T value, Container context) {
    switch (operation) {
      case GE:
      case EQ:
      case LE:
        return context.iand(sets.getOrDefault(value, EMPTY));
      case LT:
        Map.Entry<T, Container> higher = sets.higherEntry(value);
        return context.iand(null == higher ? EMPTY : higher.getValue());
      case GT:
        Map.Entry<T, Container> lower = sets.lowerEntry(value);
        return context.iand(null == lower ? EMPTY : lower.getValue());
      default:
        return context;
    }
  }

  public ComparableNode<T> optimise() {
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
    Container prev = null;
    for (Map.Entry<T, Container> set : sets.entrySet()) {
      if (prev != null) {
        sets.put(set.getKey(), set.getValue().ior(prev));
      }
      prev = set.getValue();
    }
  }

  private void reverseRangeEncode() {
    Container prev = null;
    for (Map.Entry<T, Container> set : sets.descendingMap().entrySet()) {
      if (prev != null) {
        sets.put(set.getKey(), set.getValue().ior(prev));
      }
      prev = set.getValue();
    }
  }
}
