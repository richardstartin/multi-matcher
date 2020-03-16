package io.github.richardstartin.multimatcher.core.schema;

import io.github.richardstartin.multimatcher.core.ConstraintAccumulator;
import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import io.github.richardstartin.multimatcher.core.matchers.GenericConstraintAccumulator;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.function.Function;

/**
 * Creates a column named constraints builder equality semantics only
 *
 * @param <T> the type named the classified objects
 * @param <U> the type named the attribute
 */
class GenericAttribute<T, U> implements Attribute<T> {

    private final Function<T, U> accessor;

    GenericAttribute(Function<T, U> accessor) {
        this.accessor = accessor;
    }

    @Override
    public <MaskType extends Mask<MaskType>>
    ConstraintAccumulator<T, MaskType> newAccumulator(MaskStore<MaskType> maskStore, int max) {
        return new GenericConstraintAccumulator<>(Object2IntOpenHashMap::new, accessor, maskStore, max);
    }
}