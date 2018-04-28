package uk.co.openkappa.bitrules.nodes;

import org.roaringbitmap.ArrayContainer;
import org.roaringbitmap.Container;
import org.roaringbitmap.RunContainer;

import java.util.HashMap;
import java.util.Map;

public class GenericEqualityNode<T> {

  private static final Container EMPTY = new RunContainer();

  private final Map<T, Container> segments = new HashMap<>();
  private Container wildcard = RunContainer.full();


  public void add(T segment, short priority) {
    segments.compute(segment, (seg, priorities) -> null == priorities ? new ArrayContainer().add(priority) : priorities.add(priority));
    wildcard = wildcard.remove(priority);
  }

  public Container apply(T value, Container result) {
    return result.iand(segments.getOrDefault(value, EMPTY).or(wildcard));
  }
}
