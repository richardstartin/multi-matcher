package com.openkappa.bitrules.nodes;

import com.openkappa.bitrules.Constraint;
import com.openkappa.bitrules.Rule;
import org.roaringbitmap.ArrayContainer;
import org.roaringbitmap.Container;
import org.roaringbitmap.RunContainer;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

public class ComparableRule<T, U> implements Rule<T> {

  private static final Container EMPTY = new ArrayContainer();

  private final Function<T, U> accessor;
  private final SortedMap<U, Container> sets;
  private Container wildcards = RunContainer.full();

  public ComparableRule(Function<T, U> accessor, Comparator<U> comparator) {
    this.accessor = accessor;
    this.sets = new TreeMap<>(comparator);
  }

  @Override
  public Container match(T value, Container context) {
    return context.iand(sets.getOrDefault(accessor.apply(value), EMPTY).or(wildcards));
  }

  @Override
  public void addConstraint(Constraint constraint, short priority) {
    sets.compute((U)constraint.getValue(), (k, v) -> null == v ? EMPTY.clone().add(priority) : v.add(priority));
    wildcards = wildcards.remove(priority);
  }
}
