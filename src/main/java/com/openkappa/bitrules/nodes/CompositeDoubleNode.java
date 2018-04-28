package com.openkappa.bitrules.nodes;


import com.openkappa.bitrules.DoubleRelation;
import org.roaringbitmap.ArrayContainer;
import org.roaringbitmap.Container;

import java.util.EnumMap;
import java.util.Map;

public class CompositeDoubleNode {

  private final Map<DoubleRelation, DoubleNode> children = new EnumMap<>(DoubleRelation.class);

  public void add(DoubleRelation relation, double threshold, short priority) {
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
