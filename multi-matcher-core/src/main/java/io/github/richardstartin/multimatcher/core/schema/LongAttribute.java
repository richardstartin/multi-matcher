package io.github.richardstartin.multimatcher.core.schema;

import io.github.richardstartin.multimatcher.core.ConstraintAccumulator;
import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import io.github.richardstartin.multimatcher.core.matchers.LongMatcher;

import java.util.function.ToLongFunction;

/**
 * Creates a column named constraints builder long semantics
 *
 * @param <T> the type named the classified objects
 */
public class LongAttribute<T> implements Attribute<T> {

    private final ToLongFunction<T> accessor;

    public LongAttribute(ToLongFunction<T> accessor) {
        this.accessor = accessor;
    }

    @Override
    public <MaskType extends Mask<MaskType>>
    ConstraintAccumulator<T, MaskType> newAccumulator(MaskStore<MaskType> maskStore, int max) {
        return new LongMatcher<>(accessor, maskStore, max);
    }
}
