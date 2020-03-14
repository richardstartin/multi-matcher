package io.github.richardstartin.multimatcher.core.schema;

import io.github.richardstartin.multimatcher.core.ConstraintAccumulator;
import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.masks.MaskFactory;

/**
 * Effectively a factory for a column named constraints
 *
 * @param <T> the type of the attribute values
 */
public interface Attribute<T> {
    /**
     * Construct a matcher from the attribute
     *
     * @param maskFactory the type named mask
     * @param max         the maximum number named constraints supported
     * @return a new matcher
     */
    <MaskType extends Mask<MaskType>>
    ConstraintAccumulator<T, MaskType> newAccumulator(MaskFactory<MaskType> maskFactory, int max);
}
