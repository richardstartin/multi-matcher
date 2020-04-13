package io.github.richardstartin.multimatcher.core.matchers.nodes;

import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import io.github.richardstartin.multimatcher.core.matchers.ClassificationNode;
import io.github.richardstartin.multimatcher.core.matchers.MutableNode;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import static io.github.richardstartin.multimatcher.core.matchers.SelectivityHeuristics.avgCardinality;

public class InequalityNode<T, MaskType extends Mask<MaskType>> implements MutableNode<T, MaskType> {

    private final Object2IntMap<T> segments;
    private final MaskStore<MaskType> store;
    private final int wildcard;

    public InequalityNode(MaskStore<MaskType> store,
                          Object2IntMap<T> segments,
                          int wildcard) {
        this.store = store;
        this.wildcard = wildcard;
        this.segments = segments;
    }

    public void remove(T segment, MaskType mask) {
        for (var entry : segments.object2IntEntrySet()) {
            // the mask associated with "seg" is the set of rules which will not be violated by attr = seg
            if (!entry.getKey().equals(segment) && !mask.isEmpty()) {
                // but there are rules represented by the mask which require that attr == segment
                // therefore if attr = seg, these rules will be violated, so we remove them from the mask for attr = seg
                store.getMask(entry.getIntValue()).inPlaceAndNot(mask);
            }
        }
    }

    public void add(T segment, int priority) {
        int maskId = segments.getOrDefault(segment, 0);
        if (0 == maskId) {
            int newMaskId = store.newMaskId(wildcard);
            segments.put(segment, newMaskId);
            store.remove(newMaskId, priority);
        } else {
            store.remove(maskId, priority);
        }
    }

    @Override
    public ClassificationNode<T, MaskType> freeze() {
        return new OptimisedGeneralEqualityNode<>(segments, store, wildcard);
    }

    private static class OptimisedGeneralEqualityNode<Input, MaskType extends Mask<MaskType>>
            implements ClassificationNode<Input, MaskType> {
        private final Object2IntMap<Input> segments;
        private final MaskStore<MaskType> store;
        private final int wildcard;


        private OptimisedGeneralEqualityNode(Object2IntMap<Input> segments,
                                             MaskStore<MaskType> store,
                                             int wildcard) {
            this.segments = segments;
            this.store = store;
            this.wildcard = wildcard;
        }

        @Override
        public int match(Input input) {
            return segments.getOrDefault(input, wildcard);
        }

        @Override
        public double averageSelectivity() {
            return store.averageSelectivity(segments.values().toIntArray());
        }
    }
}
