package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.Mask;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static uk.co.openkappa.bitrules.Mask.with;

class GenericEqualityNode<T, MaskType extends Mask<MaskType>> implements Node<T, MaskType> {

  private final Map<T, MaskType> segments;
  private final MaskType empty;
  private final MaskType wildcard;

  public GenericEqualityNode(Map<T, MaskType> segments, MaskType empty, MaskType wildcard) {
    this.empty = empty;
    this.wildcard = wildcard;
    this.segments = segments;
  }

  public void add(T segment, int priority) {
    segments.compute(segment, (seg, priorities) -> null == priorities ? maskWith(priority) : with(priorities, priority));
  }

  @Override
  public MaskType match(T value, MaskType result) {
    return result.inPlaceAnd(segments.getOrDefault(value, empty).or(wildcard));
  }

  @Override
  public void optimise() {
    for (Map.Entry<T, MaskType> segment: segments.entrySet()) {
      segment.getValue().optimise();
    }
  }

  private MaskType maskWith(int priority) {
    MaskType mask = empty.clone();
    mask.add(priority);
    return mask;
  }
}
