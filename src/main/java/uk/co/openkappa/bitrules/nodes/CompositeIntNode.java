package uk.co.openkappa.bitrules.nodes;

import uk.co.openkappa.bitrules.Operation;
import org.roaringbitmap.ArrayContainer;
import org.roaringbitmap.Container;

import java.util.EnumMap;
import java.util.Map;

public class CompositeIntNode {

  private final Map<Operation, IntNode> children = new EnumMap<>(Operation.class);

  public void add(Operation relation, int threshold, short priority) {
    children.computeIfAbsent(relation, IntNode::new).add(threshold, priority);
  }

  public Container match(int value, Container result) {
    Container temp = new ArrayContainer();
    for (IntNode component : children.values()) {
      temp = temp.ior(component.apply(value, result.clone()));
    }
    return result.iand(temp);
  }

  public void optimise() {
    children.values().forEach(IntNode::optimise);
  }
}
