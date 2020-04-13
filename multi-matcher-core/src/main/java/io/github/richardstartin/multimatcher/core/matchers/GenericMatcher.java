package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.Matcher;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.function.Function;

class GenericMatcher<T, U, MaskType extends Mask<MaskType>> implements Matcher<T, MaskType> {

    private final Function<T, U> accessor;
    private final Object2IntMap<U> masks;
    private final int wildcard;
    private final MaskStore<MaskType> store;

    GenericMatcher(MaskStore<MaskType> store,
                   Function<T, U> accessor,
                   Object2IntMap<U> masks,
                   int wildcard) {
        this.accessor = accessor;
        this.masks = masks;
        this.wildcard = wildcard;
        this.store = store;
    }

    @Override
    public void match(T input, MaskType context) {
        U value = accessor.apply(input);
        int mask = masks.getOrDefault(value, wildcard);
        store.andInto(context, mask);
    }

    @Override
    public float averageSelectivity() {
        return (float)store.averageSelectivity(masks.values().toIntArray());
    }

}
