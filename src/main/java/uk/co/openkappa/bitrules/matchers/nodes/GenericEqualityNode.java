package uk.co.openkappa.bitrules.matchers.nodes;

import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.matchers.ClassificationNode;
import uk.co.openkappa.bitrules.matchers.MutableNode;
import uk.co.openkappa.bitrules.matchers.SelectivityHeuristics;

import java.util.Map;
import java.util.function.Function;

import static uk.co.openkappa.bitrules.Mask.with;

public class GenericEqualityNode<T, MaskType extends Mask<MaskType>> implements MutableNode<T, MaskType> {

  private final Function<Map<T, MaskType>, Map<T, MaskType>> segmentOptimiser;
  private final Map<T, MaskType> segments;
  private final MaskType empty;
  private final MaskType wildcard;

  public GenericEqualityNode(Map<T, MaskType> segments, MaskType empty, MaskType wildcard) {
    this(segments, empty, wildcard, Function.identity());
  }

  public GenericEqualityNode(Map<T, MaskType> segments,
                             MaskType empty,
                             MaskType wildcard,
                             Function<Map<T, MaskType>, Map<T, MaskType>> segmentOptimiser) {
    this.empty = empty;
    this.wildcard = wildcard;
    this.segments = segments;
    this.segmentOptimiser = segmentOptimiser;
  }

  public void add(T segment, int priority) {
    segments.compute(segment, (seg, priorities) -> null == priorities ? maskWith(priority) : with(priorities, priority));
  }

  @Override
  public MaskType match(T value, MaskType result) {
    return result.inPlaceAnd(segments.getOrDefault(value, empty).or(wildcard));
  }

  @Override
  public ClassificationNode<T, MaskType> optimise() {
    wildcard.optimise();
    return new OptimisedGeneralEqualityNode<>(segmentOptimiser.apply(segments), empty, wildcard);
  }

  @Override
  public float averageSelectivity() {
    return SelectivityHeuristics.avgCardinality(segments.values());
  }

  private MaskType maskWith(int priority) {
    MaskType mask = empty.clone();
    mask.add(priority);
    return mask;
  }

  private static class OptimisedGeneralEqualityNode<Input, MaskType extends Mask<MaskType>>
          implements ClassificationNode<Input, MaskType> {
    private final Map<Input, MaskType> segments;
    private final MaskType empty;
    private final MaskType wildcard;


    private OptimisedGeneralEqualityNode(Map<Input, MaskType> segments,
                                         MaskType empty,
                                         MaskType wildcard) {
      this.segments = segments;
      this.empty = empty;
      this.wildcard = wildcard;
    }

    @Override
    public MaskType match(Input input, MaskType context) {
      return context.inPlaceAnd(segments.getOrDefault(input, empty).or(wildcard));
    }
  }
}
