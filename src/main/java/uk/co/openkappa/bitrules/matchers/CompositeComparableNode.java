package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Operation;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;

import static uk.co.openkappa.bitrules.matchers.Masks.singleton;

public class CompositeComparableNode<T, MaskType extends Mask<MaskType>> {

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
