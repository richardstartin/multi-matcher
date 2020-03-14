package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.*;
import io.github.richardstartin.multimatcher.core.masks.MaskFactory;
import io.github.richardstartin.multimatcher.core.matchers.nodes.DoubleNode;

import java.util.function.ToDoubleFunction;

import static io.github.richardstartin.multimatcher.core.matchers.SelectivityHeuristics.avgCardinality;
import static io.github.richardstartin.multimatcher.core.Utils.newArray;
import static io.github.richardstartin.multimatcher.core.Utils.nullCount;

public class DoubleMatcher<T, MaskType extends Mask<MaskType>> implements ConstraintAccumulator<T, MaskType>,
        Matcher<T, MaskType> {

  private final ToDoubleFunction<T> accessor;
  private DoubleNode<MaskType>[] children;
  private final MaskFactory<MaskType> factory;
  private final ThreadLocal<MaskType> empty;
  private final MaskType wildcards;

  @SuppressWarnings("unchecked")
  public DoubleMatcher(ToDoubleFunction<T> accessor, MaskFactory<MaskType> maskFactory, int max) {
    this.accessor = accessor;
    this.wildcards = maskFactory.contiguous(max);
    this.factory = maskFactory;
    this.empty = ThreadLocal.withInitial(factory::newMask);
    this.children = (DoubleNode<MaskType>[])newArray(DoubleNode.class, Operation.SIZE);
  }

  @Override
  public void match(T value, MaskType context) {
    MaskType temp = empty.get().inPlaceOr(wildcards);
    double d = accessor.applyAsDouble(value);
    for (var component : children) {
      temp.inPlaceOr(component.match(d, factory.emptySingleton()));
    }
    context.inPlaceAnd(temp);
    temp.clear();
  }

  @Override
  public boolean addConstraint(Constraint constraint, int priority) {
    Number number = constraint.getValue();
    double value = number.doubleValue();
    add(constraint.getOperation(), value, priority);
    wildcards.remove(priority);
    return true;
  }

  @Override
  public Matcher<T, MaskType> toMatcher() {
    optimise();
    wildcards.optimise();
    return this;
  }

  private void add(Operation relation, double threshold, int priority) {
    var existing = children[relation.ordinal()];
    if (null == existing) {
      existing  = children[relation.ordinal()]
              = new DoubleNode<>(factory, relation);
    }
    existing.add(threshold, priority);
  }

  @SuppressWarnings("unchecked")
  private void optimise() {
    int nullCount = nullCount(children);
    if (nullCount > 0) {
      var newChildren = (DoubleNode<MaskType>[]) newArray(DoubleNode.class, children.length - nullCount);
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
    return avgCardinality(children, DoubleNode::averageSelectivity);
  }

}
