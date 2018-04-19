package com.openkappa.bitrules.nodes;

import org.roaringbitmap.ArrayContainer;
import org.roaringbitmap.Container;
import org.roaringbitmap.RunContainer;

import java.util.HashMap;
import java.util.Map;

public class StringEqualityNode {

  private static final Container EMPTY = new RunContainer();

  private final Map<String, Container> segments = new HashMap<>();

  public StringEqualityNode() {
    segments.put("*", RunContainer.full());
  }

  public void add(String segment, short priority) {
    segments.compute(segment,
            (seg, priorities) -> null == priorities
                    ? new ArrayContainer().add(priority)
                    : priorities.add(priority));
    if (!"*".equals(segment)) {
      segments.put("*", segments.get("*").remove(priority));
    }
  }

  public Container apply(String value, Container result) {
    return result.iand(segments.getOrDefault(value, EMPTY).or(segments.getOrDefault("*", EMPTY)));
  }
}
