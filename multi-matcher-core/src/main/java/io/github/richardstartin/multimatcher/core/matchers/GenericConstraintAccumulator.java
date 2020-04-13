package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.*;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import io.github.richardstartin.multimatcher.core.matchers.nodes.EqualityNode;
import io.github.richardstartin.multimatcher.core.matchers.nodes.InequalityNode;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.richardstartin.multimatcher.core.Operation.EQ;
import static io.github.richardstartin.multimatcher.core.Operation.NE;
import static io.github.richardstartin.multimatcher.core.Utils.newArray;

public class GenericConstraintAccumulator<T, U, MaskType extends Mask<MaskType>>
        implements ConstraintAccumulator<T, MaskType> {

    protected final Function<T, U> accessor;
    protected final Supplier<Object2IntMap<U>> mapSupplier;
    protected final Map<U, MaskType> equality;
    protected final Map<U, MaskType> inequality;
    protected final int max;
    protected final MaskStore<MaskType> store;
    protected final MaskType wildcard;

    public GenericConstraintAccumulator(Supplier<Object2IntMap<U>> primitiveMapSupplier,
                                        Supplier<Map<U, MaskType>> mapSupplier,
                                        Function<T, U> accessor,
                                        MaskStore<MaskType> store,
                                        int max) {
        this.accessor = accessor;
        this.mapSupplier = primitiveMapSupplier;
        this.store = store;
        this.max = max;
        this.wildcard = store.contiguous(max);
        this.equality = mapSupplier.get();
        this.inequality = mapSupplier.get();
    }

    @Override
    public boolean addConstraint(Constraint constraint, int priority) {
        U key = constraint.getValue();
        if (constraint.getOperation() == EQ) {
            update(equality, key, priority);
            wildcard.remove(priority);
        } else if (constraint.getOperation() == NE) {
            update(inequality, key, priority);
        } else {
            return false;
        }
        return true;
    }

    private void update(Map<U, MaskType> map, U key, int priority) {
        var mask = map.get(key);
        if (null == mask) {
            mask = store.newMask();
            map.put(key, mask);
        }
        mask.add(priority);
    }

    @Override
    public Matcher<T, MaskType> toMatcher() {
        var masks = computeLiteralMasks();
        return new GenericMatcher<>(store, accessor, masks, store.storeMask(wildcard));
    }

    protected Object2IntMap<U> computeLiteralMasks() {
        // For each inequality mask, need to add the bits to each equality mask for mismatching values.
        // Then when lookups are done by equality, we will automatically get bits for NOT
        // constraints whenever the input matches any indexed value.
        //
        // Values with inequality constraints must not hit the wildcard, so
        // the complement of the inequality mask must be added to the equality masks,
        // but the wildcard does not require modification for inequality masks.
        //
        // This all means only one lookup needs to be done.
        // now process the relationships between equality and inequality masks
        if (!wildcard.isEmpty()) {
            for (var eq : equality.entrySet()) {
                eq.getValue()
                        .inPlaceOr(wildcard);
            }
        }
        var it = inequality.entrySet().iterator();
        while (it.hasNext()) {
            var ineq = it.next();
            boolean hasCounterpart = false;
            for (var eq : equality.entrySet()) {
                if (!eq.getKey().equals(ineq.getKey())) {
                    eq.getValue().inPlaceOr(ineq.getValue());
                } else {
                    hasCounterpart = true;
                    eq.getValue().inPlaceAndNot(ineq.getValue());
                }
            }
            if (hasCounterpart) {
                // use the equality mask instead
                it.remove();
            } else {
                // complement and optimise the mask, will use it as an equality mask
                ineq.getValue()
                    .inPlaceNot(max)
                    .inPlaceAnd(wildcard)
                    .optimise();
            }
        }
        // put the processed masks in the store
        Object2IntMap<U> masks = mapSupplier.get();
        for (var eq : equality.entrySet()) {
            var mask = eq.getValue();
            mask.optimise();
            masks.put(eq.getKey(), store.storeMask(mask));
        }
        wildcard.optimise();
        for (var ineq : inequality.entrySet()) {
            masks.put(ineq.getKey(), store.storeMask(ineq.getValue()));
        }
        return masks;
    }

}
