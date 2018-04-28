package uk.co.openkappa.bitrules.nodes;

import uk.co.openkappa.bitrules.Operation;
import org.roaringbitmap.ArrayContainer;
import org.roaringbitmap.Container;

import java.util.EnumMap;
import java.util.Map;

public class CompositeLongNode {

  private final Map<Operation, LongNode> children = new EnumMap<>(Operation.class);

  public void add(Operation relation, long threshold, short priority) {
    children.computeIfAbsent(relation, LongNode::new).add(threshold, priority);
  }

  public Container match(long value, Container result) {
    Container temp = new ArrayContainer();
    for (LongNode component : children.values()) {
      temp = temp.ior(component.apply(value, result.clone()));
    }
    return result.iand(temp);
  }

  public void optimise() {
    children.values().forEach(LongNode::optimise);
  }

}
