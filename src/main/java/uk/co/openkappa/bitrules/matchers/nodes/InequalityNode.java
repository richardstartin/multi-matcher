package uk.co.openkappa.bitrules.matchers.nodes;

import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.matchers.ClassificationNode;
import uk.co.openkappa.bitrules.matchers.MutableNode;

import java.util.Map;
import java.util.function.Function;

import static uk.co.openkappa.bitrules.Mask.without;
import static uk.co.openkappa.bitrules.matchers.SelectivityHeuristics.avgCardinality;

public class InequalityNode<T, MaskType extends Mask<MaskType>> implements MutableNode<T, MaskType> {

  private final Function<Map<T, MaskType>, Map<T, MaskType>> segmentOptimiser;
  private final Map<T, MaskType> segments;
  private final MaskType wildcard;

  public InequalityNode(Map<T, MaskType> segments, MaskType wildcard) {
    this(segments, wildcard, Function.identity());
  }

  public InequalityNode(Map<T, MaskType> segments,
                        MaskType wildcard,
                        Function<Map<T, MaskType>, Map<T, MaskType>> segmentOptimiser) {
    this.wildcard = wildcard;
    this.segments = segments;
    this.segmentOptimiser = segmentOptimiser;
  }

  public void remove(T segment, MaskType mask) {
    segments.forEach((seg, priorities) -> {
      // the mask associated with "seg" is the set of rules which will not be violated by attr = seg
      if (!seg.equals(segment) && !mask.isEmpty()) {
        // but there are rules represented by the mask which require that attr == segment
        // therefore if attr = seg, these rules will be violated, so we remove them from the mask for attr = seg
        priorities.inPlaceAndNot(mask);
      }
    });
  }

  public void add(T segment, int priority) {
    segments.compute(segment, (seg, priorities) -> null == priorities
            ? newMaskWithout(priority)
            : without(priorities, priority));
  }

  @Override
  public MaskType match(T value) {
    return segments.getOrDefault(value, wildcard);
  }

  @Override
  public ClassificationNode<T, MaskType> freeze() {
    wildcard.optimise();
    return new OptimisedGeneralEqualityNode<>(segmentOptimiser.apply(segments), wildcard);
  }

  @Override
  public float averageSelectivity() {
    return avgCardinality(segments.values());
  }

  private MaskType newMaskWithout(int priority) {
    return without(wildcard.clone(), priority);
  }

  private static class OptimisedGeneralEqualityNode<Input, MaskType extends Mask<MaskType>>
          implements ClassificationNode<Input, MaskType> {
    private final Map<Input, MaskType> segments;
    private final MaskType wildcard;


    private OptimisedGeneralEqualityNode(Map<Input, MaskType> segments,
                                         MaskType wildcard) {
      this.segments = segments;
      this.wildcard = wildcard;
    }

    @Override
    public MaskType match(Input input) {
      return segments.getOrDefault(input, wildcard);
    }
  }
}
