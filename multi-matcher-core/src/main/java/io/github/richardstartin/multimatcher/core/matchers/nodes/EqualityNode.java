package io.github.richardstartin.multimatcher.core.matchers.nodes;

import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.Operation;
import io.github.richardstartin.multimatcher.core.matchers.ClassificationNode;
import io.github.richardstartin.multimatcher.core.matchers.MutableNode;

import java.util.Map;
import java.util.function.Function;

import static io.github.richardstartin.multimatcher.core.Mask.with;
import static io.github.richardstartin.multimatcher.core.Operation.NE;
import static io.github.richardstartin.multimatcher.core.matchers.SelectivityHeuristics.avgCardinality;

public class EqualityNode<T, MaskType extends Mask<MaskType>> implements MutableNode<T, MaskType> {

  private final Function<Map<T, MaskType>, Map<T, MaskType>> segmentOptimiser;
  private final Map<T, MaskType> segments;
  private final MaskType empty;

  public EqualityNode(Map<T, MaskType> segments, MaskType empty) {
    this(segments, empty, Function.identity());
  }

  public EqualityNode(Map<T, MaskType> segments,
                      MaskType empty,
                      Function<Map<T, MaskType>, Map<T, MaskType>> segmentOptimiser) {
    this.empty = empty;
    this.segments = segments;
    this.segmentOptimiser = segmentOptimiser;
  }

  public void add(T segment, int priority) {
    segments.compute(segment, (seg, priorities) -> null == priorities ? maskWith(priority) : with(priorities, priority));
  }

  @Override
  public ClassificationNode<T, MaskType> freeze() {
    return new OptimisedGeneralEqualityNode<>(segmentOptimiser.apply(segments), empty);
  }

  @Override
  public void link(Map<Operation, MutableNode<T, MaskType>> nodes) {
    var inequalityNode = nodes.get(NE);
    if (inequalityNode instanceof InequalityNode) {
      var node = (InequalityNode<T, MaskType>)inequalityNode;
      segments.forEach(node::remove);
    }
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


    private OptimisedGeneralEqualityNode(Map<Input, MaskType> segments, MaskType empty) {
      this.segments = segments;
      this.empty = empty;
    }

    @Override
    public MaskType match(Input input) {
      return segments.getOrDefault(input, empty);
    }

    @Override
    public float averageSelectivity() {
      return avgCardinality(segments.values());
    }
  }
}
