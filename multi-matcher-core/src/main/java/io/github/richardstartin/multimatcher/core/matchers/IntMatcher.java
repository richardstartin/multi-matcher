package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.*;
import io.github.richardstartin.multimatcher.core.masks.MaskFactory;
import io.github.richardstartin.multimatcher.core.matchers.nodes.IntNode;

import java.util.function.ToIntFunction;

import static io.github.richardstartin.multimatcher.core.matchers.SelectivityHeuristics.avgCardinality;
import static io.github.richardstartin.multimatcher.core.matchers.Utils.newArray;
import static io.github.richardstartin.multimatcher.core.matchers.Utils.nullCount;

public class IntMatcher<T, MaskType extends Mask<MaskType>> implements ConstraintAccumulator<T, MaskType>,
        Matcher<T, MaskType> {

  private final ToIntFunction<T> accessor;
  private IntNode<MaskType>[] children;
  private final MaskType wildcards;
  private final ThreadLocal<MaskType> empty;
  private final MaskFactory<MaskType> factory;

  @SuppressWarnings("unchecked")
  public IntMatcher(ToIntFunction<T> accessor, MaskFactory<MaskType> maskFactory, int max) {
    this.accessor = accessor;
    this.factory = maskFactory;
    this.empty = ThreadLocal.withInitial(factory::newMask);
    this.wildcards = maskFactory.contiguous(max);
    this.children = (IntNode<MaskType>[])newArray(IntNode.class, Operation.SIZE);
  }

  @Override
  public void match(T value, MaskType context) {
    MaskType temp = empty.get().inPlaceOr(wildcards);
    int i = accessor.applyAsInt(value);
    for (var component : children) {
      temp.inPlaceOr(component.apply(i, factory.emptySingleton()));
    }
    context.inPlaceAnd(temp);
    temp.clear();
  }

  @Override
  public boolean addConstraint(Constraint constraint, int priority) {
    Number number = constraint.getValue();
    int value = number.intValue();
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

  public float averageSelectivity() {
    return avgCardinality(children, IntNode::averageSelectivity);
  }

  private void add(Operation relation, int threshold, int priority) {
    var existing = children[relation.ordinal()];
    if (null == existing) {
      existing  = children[relation.ordinal()]
                = new IntNode<>(factory, relation);
    }
    existing.add(threshold, priority);
  }

  @SuppressWarnings("unchecked")
  private void optimise() {
    int nullCount = nullCount(children);
    if (nullCount > 0) {
      var newChildren = (IntNode<MaskType>[]) newArray(IntNode.class, children.length - nullCount);
      int i = 0;
      for (var child : children) {
        if (null != child) {
          newChildren[i++] = child.optimise();
        }
      }
      children = newChildren;
    }
  }


}
