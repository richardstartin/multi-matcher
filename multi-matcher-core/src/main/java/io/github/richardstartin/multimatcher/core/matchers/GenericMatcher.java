package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.Matcher;
import io.github.richardstartin.multimatcher.core.Operation;

import java.util.EnumMap;
import java.util.function.Function;

import static io.github.richardstartin.multimatcher.core.matchers.SelectivityHeuristics.avgCardinality;

class GenericMatcher<T, U, MaskType extends Mask<MaskType>> implements Matcher<T, MaskType> {

  private final Function<T, U> accessor;
  private final ClassificationNode<U, MaskType>[] nodes;
  private final MaskType wildcard;

  GenericMatcher(Function<T, U> accessor,
                 ClassificationNode<U, MaskType>[] nodes,
                 MaskType wildcard) {
    this.accessor = accessor;
    this.nodes = nodes;
    this.wildcard = wildcard;
  }

  @Override
  public void match(T input, MaskType context) {
    U value = accessor.apply(input);
    for (var node : nodes) {
      context.inPlaceAnd(node.match(value));
    }
    context.inPlaceOr(wildcard);
  }

  @Override
  public float averageSelectivity() {
    return avgCardinality(nodes, ClassificationNode::averageSelectivity);
  }
}
