package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.*;
import io.github.richardstartin.multimatcher.core.masks.MaskFactory;
import io.github.richardstartin.multimatcher.core.matchers.nodes.IntNode;
import io.github.richardstartin.multimatcher.core.matchers.nodes.LongNode;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.ToLongFunction;

import static io.github.richardstartin.multimatcher.core.matchers.SelectivityHeuristics.avgCardinality;
import static io.github.richardstartin.multimatcher.core.matchers.Utils.newArray;
import static io.github.richardstartin.multimatcher.core.matchers.Utils.nullCount;

public class LongMatcher<T, MaskType extends Mask<MaskType>> implements ConstraintAccumulator<T, MaskType>,
        Matcher<T, MaskType> {

  private final ToLongFunction<T> accessor;
  private LongNode<MaskType>[] children = (LongNode<MaskType>[])newArray(LongNode.class, Operation.SIZE);
  private final ThreadLocal<MaskType> empty;
  private final MaskType wildcards;
  private final MaskType emptySingleton;

  public LongMatcher(ToLongFunction<T> accessor, MaskFactory<MaskType> maskFactory, int max) {
    this.accessor = accessor;
    this.emptySingleton = maskFactory.emptySingleton();
    this.empty = ThreadLocal.withInitial(emptySingleton::clone);
    this.wildcards = maskFactory.contiguous(max);
  }

  @Override
  public void match(T value, MaskType context) {
    MaskType temp = empty.get().inPlaceOr(wildcards);
    long l = accessor.applyAsLong(value);
    for (var component : children) {
      temp.inPlaceOr(component.apply(l, emptySingleton));
    }
    context.inPlaceAnd(temp);
    temp.clear();
  }

  @Override
  public boolean addConstraint(Constraint constraint, int priority) {
    Number number = constraint.getValue();
    long value = number.longValue();
    add(constraint.getOperation(), value, priority);
    wildcards.remove(priority);
    return true;
  }

  @Override
  public Matcher<T, MaskType> freeze() {
    optimise();
    wildcards.optimise();
    return this;
  }


  private void add(Operation relation, long threshold, int priority) {
    var existing = children[relation.ordinal()];
    if (null == existing) {
      existing  = children[relation.ordinal()]
              = new LongNode<>(relation, emptySingleton);
    }
    existing.add(threshold, priority);
  }

  private void optimise() {
    int nullCount = nullCount(children);
    if (nullCount > 0) {
      var newChildren = (LongNode<MaskType>[]) newArray(LongNode.class, children.length - nullCount);
      int i = 0;
      for (var child : children) {
        if (null != child) {
          newChildren[i++] = child.optimise();
        }
      }
      children = newChildren;
    }
  }

  @Override
  public float averageSelectivity() {
    return avgCardinality(children, LongNode::averageSelectivity);
  }

}
