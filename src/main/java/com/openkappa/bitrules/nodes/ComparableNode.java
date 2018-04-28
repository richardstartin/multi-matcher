package com.openkappa.bitrules.nodes;

import com.openkappa.bitrules.Operation;
import org.roaringbitmap.ArrayContainer;
import org.roaringbitmap.Container;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

public class ComparableNode<T> {

  private static final Container EMPTY = new ArrayContainer();

  private final SortedMap<T, Container> sets;
  private final Operation operation;

  public ComparableNode(Comparator<T> comparator, Operation operation) {
    this.sets = new TreeMap<>(comparator);
    this.operation = operation;
  }

  public void add(T value, short priority) {
    sets.compute(value, (k, v) -> v == null ? EMPTY.clone().add(priority) : v.add(priority));
  }

  public Container match(T value, Container context) {
    return context.iand(sets.getOrDefault(value, EMPTY));
  }

  public void optimise() {

  }
}
