package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.*;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import io.github.richardstartin.multimatcher.core.matchers.nodes.DoubleNode;

import java.util.function.ToDoubleFunction;

import static io.github.richardstartin.multimatcher.core.Utils.newArray;
import static io.github.richardstartin.multimatcher.core.Utils.nullCount;
import static io.github.richardstartin.multimatcher.core.matchers.SelectivityHeuristics.avgCardinality;

public class DoubleMatcher<T, MaskType extends Mask<MaskType>> implements ConstraintAccumulator<T, MaskType>,
        Matcher<T, MaskType> {

    private final ToDoubleFunction<T> accessor;
    private final MaskStore<MaskType> store;
    private final int wildcards;
    private DoubleNode<MaskType>[] children;

    @SuppressWarnings("unchecked")
    public DoubleMatcher(ToDoubleFunction<T> accessor, MaskStore<MaskType> maskStore, int max) {
        this.accessor = accessor;
        this.wildcards = maskStore.newContiguousMaskId(max);
        this.store = maskStore;
        this.children = (DoubleNode<MaskType>[]) newArray(DoubleNode.class, Operation.SIZE);
    }

    @Override
    public void match(T value, MaskType context) {
        MaskType temp = store.getTemp(wildcards);
        double attributeValue = accessor.applyAsDouble(value);
        for (var component : children) {
            store.orInto(temp, component.match(attributeValue, -1));
        }
        context.inPlaceAnd(temp);
    }

    @Override
    public boolean addConstraint(Constraint constraint, int priority) {
        Number number = constraint.getValue();
        double value = number.doubleValue();
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

    private void add(Operation relation, double threshold, int priority) {
        var existing = children[relation.ordinal()];
        if (null == existing) {
            existing = children[relation.ordinal()]
                    = new DoubleNode<>(store, relation);
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
