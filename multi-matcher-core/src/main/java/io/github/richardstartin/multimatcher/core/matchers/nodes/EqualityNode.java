package io.github.richardstartin.multimatcher.core.matchers.nodes;

import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.Operation;
import io.github.richardstartin.multimatcher.core.masks.MaskFactory;
import io.github.richardstartin.multimatcher.core.matchers.ClassificationNode;
import io.github.richardstartin.multimatcher.core.matchers.MutableNode;

import java.util.Map;
import java.util.function.Function;

import static io.github.richardstartin.multimatcher.core.Operation.NE;
import static io.github.richardstartin.multimatcher.core.matchers.SelectivityHeuristics.avgCardinality;

public class EqualityNode<T, MaskType extends Mask<MaskType>> implements MutableNode<T, MaskType> {

  private final Function<Map<T, MaskType>, Map<T, MaskType>> segmentOptimiser;
  private final Map<T, MaskType> segments;
  private final MaskFactory<MaskType> factory;

  public EqualityNode(MaskFactory<MaskType> factory, Map<T, MaskType> segments) {
    this(factory, segments, Function.identity());
  }

  public EqualityNode(MaskFactory<MaskType> factory,
                      Map<T, MaskType> segments,
                      Function<Map<T, MaskType>, Map<T, MaskType>> segmentOptimiser) {
    this.factory = factory;
    this.segments = segments;
    this.segmentOptimiser = segmentOptimiser;
  }

  public void add(T segment, int priority) {
    MaskType priorities = segments.get(segment);
    if (null == priorities) {
      segments.put(segment, maskWith(priority));
    } else {
      priorities.add(priority);
    }
  }

  @Override
  public ClassificationNode<T, MaskType> freeze() {
    return new OptimisedGeneralEqualityNode<>(factory, segmentOptimiser.apply(segments));
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
    MaskType mask = factory.newMask();
    mask.add(priority);
    return mask;
  }

  private static class OptimisedGeneralEqualityNode<Input, MaskType extends Mask<MaskType>>
          implements ClassificationNode<Input, MaskType> {
    private final Map<Input, MaskType> segments;
    private final MaskFactory<MaskType> factory;


    private OptimisedGeneralEqualityNode(MaskFactory<MaskType> factory, Map<Input, MaskType> segments) {
      this.segments = segments;
      this.factory = factory;
    }

    @Override
    public MaskType match(Input input) {
      return segments.getOrDefault(input, factory.emptySingleton());
    }

    @Override
    public float averageSelectivity() {
      return avgCardinality(segments.values());
    }
  }
}
