package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.Constraint;
import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.Matcher;
import io.github.richardstartin.multimatcher.core.Operation;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import io.github.richardstartin.multimatcher.core.matchers.nodes.PrefixNode;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.richardstartin.multimatcher.core.Operation.*;
import static io.github.richardstartin.multimatcher.core.Utils.newArray;

public class StringConstraintAccumulator<Input, MaskType extends Mask<MaskType>>
        extends GenericConstraintAccumulator<Input, String, MaskType> {

    public StringConstraintAccumulator(Function<Input, String> accessor,
                                       MaskStore<MaskType> maskStore,
                                       int max) {
        this(Object2IntOpenHashMap::new, accessor, maskStore, max);
    }

    private StringConstraintAccumulator(Supplier<Object2IntMap<String>> mapSupplier,
                                        Function<Input, String> accessor,
                                        MaskStore<MaskType> maskStore,
                                        int max) {
        super(mapSupplier, accessor, maskStore, max);
    }

    @Override
    public boolean addConstraint(Constraint constraint, int priority) {
        if (super.addConstraint(constraint, priority)) {
            return true;
        }
        if (constraint.getOperation() == STARTS_WITH) {
            var prefix = (PrefixNode<MaskType>) nodes.computeIfAbsent(STARTS_WITH,
                    o -> new PrefixNode<>(store));
            prefix.add(constraint.getValue(), priority);
        } else {
            return false;
        }
        store.remove(wildcard, priority);
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Matcher<Input, MaskType> toMatcher() {
        store.optimise(wildcard);
        var frozen = (ClassificationNode<String, MaskType>[]) newArray(ClassificationNode.class, SIZE);
        for (var node : nodes.values()) {
            node.link(nodes);
        }
        for (var pair : nodes.entrySet()) {
            frozen[pair.getKey().ordinal()] = pair.getValue().freeze();
        }
        return new StringMatcher<>(store, accessor, frozen, wildcard);
    }

    private static class StringMatcher<T, MaskType extends Mask<MaskType>> implements Matcher<T, MaskType> {

        private final Function<T, String> accessor;
        private final ClassificationNode<String, MaskType>[] nodes;
        private final int wildcard;
        private final MaskStore<MaskType> store;

        StringMatcher(MaskStore<MaskType> store,
                      Function<T, String> accessor,
                      ClassificationNode<String, MaskType>[] nodes,
                      int wildcard) {
            this.accessor = accessor;
            this.nodes = nodes;
            this.wildcard = wildcard;
            this.store = store;
        }

        @Override
        public void match(T input, MaskType context) {
            String value = accessor.apply(input);
            var temp = store.getTemp();
            match(EQ, temp, value);
            match(STARTS_WITH, temp, value);
            matchNotEquals(context, value);
            store.orInto(temp, wildcard);
            context.inPlaceAnd(temp);
            temp.clear();
        }

        private void matchNotEquals(MaskType context, String value) {
            var node = nodes[NE.ordinal()];
            if (null != node) {
                store.andInto(context, node.match(value));
            }
        }

        private void match(Operation op, MaskType context, String value) {
            var node = nodes[op.ordinal()];
            if (null != node) {
                store.orInto(context, node.match(value));
            }
        }
    }
}
