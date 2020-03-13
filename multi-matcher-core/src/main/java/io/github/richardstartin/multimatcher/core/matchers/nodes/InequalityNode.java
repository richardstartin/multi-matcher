package io.github.richardstartin.multimatcher.core.matchers.nodes;

import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.matchers.ClassificationNode;
import io.github.richardstartin.multimatcher.core.matchers.MutableNode;

import java.util.Map;
import java.util.function.Function;

import static io.github.richardstartin.multimatcher.core.Mask.without;
import static io.github.richardstartin.multimatcher.core.matchers.SelectivityHeuristics.avgCardinality;

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
    for (var entry : segments.entrySet()) {
      // the mask associated with "seg" is the set of rules which will not be violated by attr = seg
      if (!entry.getKey().equals(segment) && !mask.isEmpty()) {
        // but there are rules represented by the mask which require that attr == segment
        // therefore if attr = seg, these rules will be violated, so we remove them from the mask for attr = seg
        entry.getValue().inPlaceAndNot(mask);
      }
    }
  }

  public void add(T segment, int priority) {
    MaskType priorities = segments.get(segment);
    if (null == priorities) {
      segments.put(segment, newMaskWithout(priority));
    } else {
      priorities.remove(priority);
    }
  }

  @Override
  public ClassificationNode<T, MaskType> freeze() {
    wildcard.optimise();
    return new OptimisedGeneralEqualityNode<>(segmentOptimiser.apply(segments), wildcard);
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

    @Override
    public float averageSelectivity() {
      return avgCardinality(segments.values());
    }
  }
}
