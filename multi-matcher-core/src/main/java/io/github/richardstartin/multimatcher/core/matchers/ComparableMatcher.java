package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.*;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import io.github.richardstartin.multimatcher.core.matchers.nodes.ComparableNode;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;

import static io.github.richardstartin.multimatcher.core.Utils.newArray;
import static io.github.richardstartin.multimatcher.core.Utils.nullCount;

public class ComparableMatcher<T, U, MaskType extends Mask<MaskType>> implements ConstraintAccumulator<T, MaskType>,
        Matcher<T, MaskType> {

    private final Function<T, U> accessor;
    private final int wildcards;
    private final Comparator<U> comparator;
    private final MaskStore<MaskType> store;
    private ComparableNode<U, MaskType>[] children;

    @SuppressWarnings("unchecked")
    public ComparableMatcher(Function<T, U> accessor,
                             Comparator<U> comparator, MaskStore<MaskType> maskStore,
                             int max) {
        this.accessor = accessor;
        this.comparator = comparator;
        this.store = maskStore;
        this.wildcards = maskStore.newContiguousMaskId(max);
        this.children = (ComparableNode<U, MaskType>[]) newArray(ComparableNode.class, Operation.SIZE);
    }

    @Override
    public void match(T value, MaskType context) {
        MaskType temp = store.getTemp(wildcards);
        U comparable = accessor.apply(value);
        for (var component : children) {
            store.orInto(temp, component.match(comparable));
        }
        context.inPlaceAnd(temp);
        temp.clear();
    }

    @Override
    public boolean addConstraint(Constraint constraint, int priority) {
        add(constraint.getOperation(), constraint.getValue(), priority);
        store.remove(wildcards, priority);
        return true;
    }

    @Override
    public Matcher<T, MaskType> toMatcher() {
        optimise();
        store.optimise(wildcards);
        return this;
    }

    @Override
    public float averageSelectivity() {
        return SelectivityHeuristics.avgCardinality(children, ComparableNode::averageSelectivity);
    }

    @Override
    public String toString() {
        return Arrays.toString(children) + ", *: " + wildcards;
    }

    private void add(Operation relation, U threshold, int priority) {
        var existing = children[relation.ordinal()];
        if (null == existing) {
            existing = children[relation.ordinal()]
                    = new ComparableNode<>(store, comparator, relation);
        }
        existing.add(threshold, priority);
    }

    @SuppressWarnings("unchecked")
    public void optimise() {
        int nullCount = nullCount(children);
        if (nullCount > 0) {
            var newChildren = (ComparableNode<U, MaskType>[]) newArray(ComparableNode.class, children.length - nullCount);
            int i = 0;
            for (var child : children) {
                if (null != child) {
                    newChildren[i++] = child.freeze();
                }
            }
            children = newChildren;
        }
    }

}
