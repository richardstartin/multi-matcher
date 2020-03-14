package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.*;
import io.github.richardstartin.multimatcher.core.masks.MaskFactory;
import io.github.richardstartin.multimatcher.core.matchers.nodes.ComparableNode;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;

import static io.github.richardstartin.multimatcher.core.Utils.newArray;
import static io.github.richardstartin.multimatcher.core.Utils.nullCount;

public class ComparableMatcher<T, U, MaskType extends Mask<MaskType>> implements ConstraintAccumulator<T, MaskType>,
        Matcher<T, MaskType> {

    private final Function<T, U> accessor;
    private final MaskType wildcards;
    private final Comparator<U> comparator;
    private final MaskFactory<MaskType> factory;
    private final ThreadLocal<MaskType> empty;
    private ComparableNode<U, MaskType>[] children;

    @SuppressWarnings("unchecked")
    public ComparableMatcher(Function<T, U> accessor,
                             Comparator<U> comparator, MaskFactory<MaskType> maskFactory,
                             int max) {
        this.accessor = accessor;
        this.comparator = comparator;
        this.factory = maskFactory;
        this.wildcards = maskFactory.contiguous(max);
        this.empty = ThreadLocal.withInitial(factory::newMask);
        this.children = (ComparableNode<U, MaskType>[]) newArray(ComparableNode.class, Operation.SIZE);
    }

    @Override
    public void match(T value, MaskType context) {
        MaskType temp = empty.get().inPlaceOr(wildcards);
        U comparable = accessor.apply(value);
        for (var component : children) {
            temp = temp.inPlaceOr(component.match(comparable));
        }
        context.inPlaceAnd(temp);
        temp.clear();
    }

    @Override
    public boolean addConstraint(Constraint constraint, int priority) {
        add(constraint.getOperation(), constraint.getValue(), priority);
        wildcards.remove(priority);
        return true;
    }

    @Override
    public Matcher<T, MaskType> toMatcher() {
        optimise();
        wildcards.optimise();
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
                    = new ComparableNode<>(factory, comparator, relation);
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
