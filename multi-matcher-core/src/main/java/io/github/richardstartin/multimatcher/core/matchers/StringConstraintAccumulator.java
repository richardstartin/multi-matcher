package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;

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
        super(mapSupplier, HashMap::new, accessor, maskStore, max);
    }
}
