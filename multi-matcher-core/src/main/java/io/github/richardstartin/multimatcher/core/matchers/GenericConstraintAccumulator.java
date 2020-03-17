package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.*;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import io.github.richardstartin.multimatcher.core.matchers.nodes.EqualityNode;
import io.github.richardstartin.multimatcher.core.matchers.nodes.InequalityNode;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.EnumMap;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.richardstartin.multimatcher.core.Utils.newArray;

public class GenericConstraintAccumulator<T, U, MaskType extends Mask<MaskType>>
        implements ConstraintAccumulator<T, MaskType> {

    protected final Function<T, U> accessor;
    protected final Supplier<Object2IntMap<U>> mapSupplier;
    protected final EnumMap<Operation, MutableNode<U, MaskType>> nodes = new EnumMap<>(Operation.class);
    protected final int wildcard;
    protected final int max;
    protected final MaskStore<MaskType> store;

    public GenericConstraintAccumulator(Supplier<Object2IntMap<U>> mapSupplier,
                                        Function<T, U> accessor,
                                        MaskStore<MaskType> store,
                                        int max) {
        this.accessor = accessor;
        this.mapSupplier = mapSupplier;
        this.wildcard = store.newContiguousMaskId(max);
        this.store = store;
        this.max = max;
    }

    @Override
    public boolean addConstraint(Constraint constraint, int priority) {
        switch (constraint.getOperation()) {
            case NE:
                ((InequalityNode<U, MaskType>) nodes
                        .computeIfAbsent(constraint.getOperation(),
                                op -> new InequalityNode<>(store, mapSupplier.get(), store.newContiguousMaskId(max))))
                        .add(constraint.getValue(), priority);
                return true;
            case EQ:
                ((EqualityNode<U, MaskType>) nodes
                        .computeIfAbsent(constraint.getOperation(),
                                op -> new EqualityNode<>(store, mapSupplier.get())))
                        .add(constraint.getValue(), priority);
                store.remove(wildcard, priority);
                return true;
            default:
                return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Matcher<T, MaskType> toMatcher() {
        store.optimise(wildcard);
        var frozen = (ClassificationNode<U, MaskType>[]) newArray(ClassificationNode.class, nodes.size());
        for (var node : nodes.values()) {
            node.link(nodes);
        }
        int i = 0;
        for (var node : nodes.values()) {
            frozen[i++] = node.freeze();
        }
        return new GenericMatcher<>(store, accessor, frozen, wildcard);
    }

}
