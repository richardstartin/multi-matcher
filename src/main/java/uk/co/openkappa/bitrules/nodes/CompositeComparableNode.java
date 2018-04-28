package uk.co.openkappa.bitrules.nodes;

import uk.co.openkappa.bitrules.Operation;
import org.roaringbitmap.ArrayContainer;
import org.roaringbitmap.Container;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;

public class CompositeComparableNode<T> {

  private final Comparator<T> comparator;
  private final Map<Operation, ComparableNode<T>> children = new EnumMap<>(Operation.class);

  public CompositeComparableNode(Comparator<T> comparator) {
    this.comparator = comparator;
  }

  public void add(Operation relation, T threshold, short priority) {
    children.computeIfAbsent(relation, r -> new ComparableNode<>(comparator, r)).add(threshold, priority);
  }

  public Container match(T value, Container result) {
    Container temp = new ArrayContainer();
    for (ComparableNode<T> component : children.values()) {
      temp = temp.ior(component.match(value, result.clone()));
    }
    return result.iand(temp);
  }

  public void optimise() {
    children.values().forEach(ComparableNode::optimise);
  }
}
