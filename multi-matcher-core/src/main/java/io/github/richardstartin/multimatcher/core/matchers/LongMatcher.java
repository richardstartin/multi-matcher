package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.*;
import io.github.richardstartin.multimatcher.core.masks.MaskFactory;
import io.github.richardstartin.multimatcher.core.matchers.nodes.LongNode;

import java.util.function.ToLongFunction;

import static io.github.richardstartin.multimatcher.core.Utils.newArray;
import static io.github.richardstartin.multimatcher.core.Utils.nullCount;
import static io.github.richardstartin.multimatcher.core.matchers.SelectivityHeuristics.avgCardinality;

public class LongMatcher<T, MaskType extends Mask<MaskType>> implements ConstraintAccumulator<T, MaskType>,
        Matcher<T, MaskType> {

    private final ToLongFunction<T> accessor;
    private final ThreadLocal<MaskType> empty;
    private final MaskType wildcards;
    private final MaskFactory<MaskType> factory;
    private LongNode<MaskType>[] children;

    @SuppressWarnings("unchecked")
    public LongMatcher(ToLongFunction<T> accessor, MaskFactory<MaskType> maskFactory, int max) {
        this.accessor = accessor;
        this.factory = maskFactory;
        this.empty = ThreadLocal.withInitial(factory::newMask);
        this.wildcards = maskFactory.contiguous(max);
        this.children = (LongNode<MaskType>[]) newArray(LongNode.class, Operation.SIZE);
    }

    @Override
    public void match(T value, MaskType context) {
        MaskType temp = empty.get().inPlaceOr(wildcards);
        long l = accessor.applyAsLong(value);
        for (var component : children) {
            temp.inPlaceOr(component.apply(l, factory.emptySingleton()));
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
    public Matcher<T, MaskType> toMatcher() {
        optimise();
        wildcards.optimise();
        return this;
    }


    private void add(Operation relation, long threshold, int priority) {
        var existing = children[relation.ordinal()];
        if (null == existing) {
            existing = children[relation.ordinal()]
                    = new LongNode<>(factory, relation);
        }
        existing.add(threshold, priority);
    }

    @SuppressWarnings("unchecked")
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
