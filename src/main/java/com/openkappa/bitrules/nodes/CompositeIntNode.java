package com.openkappa.bitrules.nodes;

import com.openkappa.bitrules.IntRelation;
import org.roaringbitmap.ArrayContainer;
import org.roaringbitmap.Container;

import java.util.EnumMap;
import java.util.Map;

public class CompositeIntNode {

  private final Map<IntRelation, IntNode> components = new EnumMap<>(IntRelation.class);

  public void add(IntRelation relation, int threshold, short priority) {
    components.computeIfAbsent(relation, IntNode::new).add(threshold, priority);
  }

  public Container apply(int value, Container result) {
    Container temp = new ArrayContainer();
    for (IntNode component : components.values()) {
      temp = temp.ior(component.apply(value, result.clone()));
    }
    return result.iand(temp);
  }
}
