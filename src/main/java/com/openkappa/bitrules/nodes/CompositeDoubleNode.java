package com.openkappa.bitrules.nodes;


import com.openkappa.bitrules.DoubleRelation;
import org.roaringbitmap.*;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class CompositeDoubleNode {

  private final Map<DoubleRelation, DoubleNode> components = new EnumMap<>(DoubleRelation.class);

  public void add(DoubleRelation relation, double threshold, short priority) {
    components.computeIfAbsent(relation, DoubleNode::new)
              .add(threshold, priority);
  }

  public Container apply(double value, Container result) {
    Container temp = new ArrayContainer();
    for (DoubleNode component : components.values()) {
      temp = temp.ior(component.apply(value, result.clone()));
    }
    return result.iand(temp);
  }

  public void optimise() {
    components.values().forEach(DoubleNode::optimise);
  }

}
