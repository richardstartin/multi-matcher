package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.*;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import io.github.richardstartin.multimatcher.core.matchers.nodes.LongNode;

import java.util.function.ToLongFunction;

import static io.github.richardstartin.multimatcher.core.Utils.newArray;
import static io.github.richardstartin.multimatcher.core.Utils.nullCount;
import static io.github.richardstartin.multimatcher.core.matchers.SelectivityHeuristics.avgCardinality;

public class LongMatcher<T, MaskType extends Mask<MaskType>> implements ConstraintAccumulator<T, MaskType>,
        Matcher<T, MaskType> {

    private final ToLongFunction<T> accessor;
    private final int wildcards;
    private final MaskStore<MaskType> store;
    private LongNode<MaskType>[] children;

    @SuppressWarnings("unchecked")
    public LongMatcher(ToLongFunction<T> accessor, MaskStore<MaskType> maskStore, int max) {
        this.accessor = accessor;
        this.store = maskStore;
        this.wildcards = maskStore.newContiguousMaskId(max);
        this.children = (LongNode<MaskType>[]) newArray(LongNode.class, Operation.SIZE);
    }

    @Override
    public void match(T value, MaskType context) {
        MaskType temp = store.getTemp(wildcards);
        long attributeValue = accessor.applyAsLong(value);
        for (var component : children) {
            store.orInto(temp, component.match(attributeValue, -1));
        }
        context.inPlaceAnd(temp);
    }

    @Override
    public boolean addConstraint(Constraint constraint, int priority) {
        Number number = constraint.getValue();
        int value = number.intValue();
        add(constraint.getOperation(), value, priority);
        store.remove(wildcards, priority);
        return true;
    }

    @Override
    public Matcher<T, MaskType> toMatcher() {
        optimise();
        store.optimise(wildcards);
        return this;
    }

    public float averageSelectivity() {
        return avgCardinality(children, LongNode::averageSelectivity);
    }

    private void add(Operation relation, int threshold, int priority) {
        var existing = children[relation.ordinal()];
        if (null == existing) {
            existing = children[relation.ordinal()] = new LongNode<>(store, relation);
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

}
