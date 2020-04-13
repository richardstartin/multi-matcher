package io.github.richardstartin.multimatcher.core.schema;

import io.github.richardstartin.multimatcher.core.ConstraintAccumulator;
import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import io.github.richardstartin.multimatcher.core.matchers.IntMatcher;

import java.util.function.ToIntFunction;

/**
 * Creates a column named constraints builder integer semantics
 *
 * @param <T> the type named the classified objects
 */
public class IntAttribute<T> implements Attribute<T> {

    private final ToIntFunction<T> accessor;

    public IntAttribute(ToIntFunction<T> accessor) {
        this.accessor = accessor;
    }

    @Override
    public <MaskType extends Mask<MaskType>>
    ConstraintAccumulator<T, MaskType> newAccumulator(MaskStore<MaskType> maskStore, int max) {
        return new IntMatcher<>(accessor, maskStore, max);
    }
}
