package io.github.richardstartin.multimatcher.core.matchers.nodes;

import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.Operation;
import io.github.richardstartin.multimatcher.core.masks.MaskFactory;
import io.github.richardstartin.multimatcher.core.matchers.ClassificationNode;
import io.github.richardstartin.multimatcher.core.matchers.MutableNode;

import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static io.github.richardstartin.multimatcher.core.matchers.SelectivityHeuristics.avgCardinality;

public class ComparableNode<T, MaskType extends Mask<MaskType>> implements MutableNode<T, MaskType>, ClassificationNode<T, MaskType> {

  private final MaskFactory<MaskType> factory;
  private final NavigableMap<T, MaskType> sets;
  private final Operation operation;

  public ComparableNode(MaskFactory<MaskType> factory,
                        Comparator<T> comparator,
                        Operation operation) {
    this.sets = new TreeMap<>(comparator);
    this.operation = operation;
    this.factory = factory;
  }

  public void add(T value, int priority) {
    sets.compute(value, (k, v) -> {
      if (v == null) {
        v = factory.newMask();
      }
      v.add(priority);
      return v;
    });
  }

  @Override
  public MaskType match(T value) {
    switch (operation) {
      case GE:
      case EQ:
      case LE:
        return sets.getOrDefault(value, factory.emptySingleton());
      case LT:
        Map.Entry<T, MaskType> higher = sets.higherEntry(value);
        return null == higher ? factory.emptySingleton() : higher.getValue();
      case GT:
        Map.Entry<T, MaskType> lower = sets.lowerEntry(value);
        return null == lower ? factory.emptySingleton() : lower.getValue();
      default:
        return factory.emptySingleton();
    }
  }

  public ComparableNode<T, MaskType> freeze() {
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

  public float averageSelectivity() {
    return avgCardinality(sets.values());
  }

  private void rangeEncode() {
    MaskType prev = null;
    for (Map.Entry<T, MaskType> set : sets.entrySet()) {
      if (prev != null) {
        var optimised = set.getValue().inPlaceOr(prev);
        optimised.optimise();
        sets.put(set.getKey(), optimised);
      }
      prev = set.getValue();
    }
  }

  private void reverseRangeEncode() {
    MaskType prev = null;
    for (var set : sets.descendingMap().entrySet()) {
      if (prev != null) {
        var optimised = set.getValue().inPlaceOr(prev);
        optimised.optimise();
        sets.put(set.getKey(), optimised);
      }
      prev = set.getValue();
    }
  }

  @Override
  public String toString() {
    return Nodes.toString(sets.size(), operation, sets);
  }
}
