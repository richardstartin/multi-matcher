package com.openkappa.bitrules.nodes;

import com.openkappa.bitrules.LongRelation;
import org.roaringbitmap.ArrayContainer;
import org.roaringbitmap.Container;

import java.util.EnumMap;
import java.util.Map;

public class CompositeLongNode {

  private final Map<LongRelation, LongNode> components = new EnumMap<>(LongRelation.class);

  public void add(LongRelation relation, long threshold, short priority) {
    components.computeIfAbsent(relation, LongNode::new).add(threshold, priority);
  }

  public Container apply(long value, Container result) {
    Container temp = new ArrayContainer();
    for (LongNode component : components.values()) {
      temp = temp.ior(component.apply(value, result.clone()));
    }
    return result.iand(temp);
  }
}
