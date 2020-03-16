package io.github.richardstartin.multimatcher.core.schema;

import io.github.richardstartin.multimatcher.core.ConstraintAccumulator;
import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import io.github.richardstartin.multimatcher.core.matchers.GenericConstraintAccumulator;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;

import java.util.function.Function;

public class EnumAttribute<E extends Enum<E>, Input> implements Attribute<Input> {

    private final Function<Input, E> accessor;
    private final Class<E> type;

    public EnumAttribute(Class<E> type, Function<Input, E> accessor) {
        this.accessor = accessor;
        this.type = type;
    }

    @Override
    public <MaskType extends Mask<MaskType>>
    ConstraintAccumulator<Input, MaskType> newAccumulator(MaskStore<MaskType> maskStore, int max) {
        return new GenericConstraintAccumulator<>(this::newMap, accessor, maskStore, max);
    }


    @SuppressWarnings("unchecked")
    private Object2IntMap<E> newMap() {
        return new Object2IntOpenCustomHashMap<>(type.getEnumConstants().length,
                1f, (Hash.Strategy<E>) STRATEGY);
    }

    private static final EnumHashStrategy<?> STRATEGY = new EnumHashStrategy<>();

    private static class EnumHashStrategy<E extends Enum<E>> implements Hash.Strategy<E> {

        @Override
        public int hashCode(E o) {
            return o.ordinal();
        }

        @Override
        public boolean equals(E a, E b) {
            return a == b;
        }
    }
}
