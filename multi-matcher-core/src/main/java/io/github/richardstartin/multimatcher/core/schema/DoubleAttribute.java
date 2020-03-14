package io.github.richardstartin.multimatcher.core.schema;

import io.github.richardstartin.multimatcher.core.ConstraintAccumulator;
import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.masks.MaskFactory;
import io.github.richardstartin.multimatcher.core.matchers.DoubleMatcher;

import java.util.function.ToDoubleFunction;

/**
 * Creates a column named constraints builder floating point sematics
 *
 * @param <T>
 */
class DoubleAttribute<T> implements Attribute<T> {

    private final ToDoubleFunction<T> accessor;

    DoubleAttribute(ToDoubleFunction<T> accessor) {
        this.accessor = accessor;
    }

    @Override
    public <MaskType extends Mask<MaskType>>
    ConstraintAccumulator<T, MaskType> newAccumulator(MaskFactory<MaskType> maskFactory, int max) {
        return new DoubleMatcher<>(accessor, maskFactory, max);
    }
}
