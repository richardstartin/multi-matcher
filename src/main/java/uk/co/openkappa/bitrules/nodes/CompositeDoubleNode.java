package uk.co.openkappa.bitrules.nodes;


import uk.co.openkappa.bitrules.Operation;
import org.roaringbitmap.ArrayContainer;
import org.roaringbitmap.Container;

import java.util.EnumMap;
import java.util.Map;

public class CompositeDoubleNode {

  private final Map<Operation, DoubleNode> children = new EnumMap<>(Operation.class);

  public void add(Operation relation, double threshold, short priority) {
    children.computeIfAbsent(relation, DoubleNode::new)
              .add(threshold, priority);
  }

  public Container match(double value, Container result) {
    Container temp = new ArrayContainer();
    for (DoubleNode component : children.values()) {
      temp = temp.ior(component.match(value, result.clone()));
    }
    return result.iand(temp);
  }

  public void optimise() {
    children.values().forEach(DoubleNode::optimise);
  }

}
