package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.Mask;

import java.util.HashMap;
import java.util.Map;

import static uk.co.openkappa.bitrules.Mask.with;

public class GenericEqualityNode<T, MaskType extends Mask<MaskType>> implements Node<T, MaskType> {

  private final Map<T, MaskType> segments = new HashMap<>();
  private final MaskType wildcard;
  private final MaskType empty;

  public GenericEqualityNode(Class<MaskType> type, MaskType empty, int max) {
    this(empty, Masks.wildcards(type, max));
  }

  public GenericEqualityNode(MaskType empty, MaskType wildcard) {
    this.wildcard = wildcard;
    this.empty = empty;
  }


  public void add(T segment, int priority) {
    segments.compute(segment, (seg, priorities) -> null == priorities ? maskWith(priority) : with(priorities, priority));
    wildcard.remove(priority);
  }

  @Override
  public MaskType match(T value, MaskType result) {
    return result.inPlaceAnd(segments.getOrDefault(value, empty).or(wildcard));
  }

  public void optimise() {
    for (Map.Entry<T, MaskType> segment: segments.entrySet()) {
      segment.getValue().optimise();
    }
    wildcard.optimise();
  }

  private MaskType maskWith(int priority) {
    MaskType mask = empty.clone();
    mask.add(priority);
    return mask;
  }
}
