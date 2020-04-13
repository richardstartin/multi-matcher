package io.github.richardstartin.multimatcher.core.matchers.nodes;

import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.Operation;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import io.github.richardstartin.multimatcher.core.matchers.ClassificationNode;
import io.github.richardstartin.multimatcher.core.matchers.MutableNode;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.Map;

import static io.github.richardstartin.multimatcher.core.Operation.NE;
import static io.github.richardstartin.multimatcher.core.matchers.SelectivityHeuristics.avgCardinality;

public class EqualityNode<T, MaskType extends Mask<MaskType>> implements MutableNode<T, MaskType> {

    private final Object2IntMap<T> segments;
    private final MaskStore<MaskType> store;

    public EqualityNode(MaskStore<MaskType> store,
                        Object2IntMap<T> segments) {
        this.store = store;
        this.segments = segments;
    }

    public void add(T segment, int priority, Operation op) {
        int maskId = segments.getOrDefault(segment, 0);
        if (0 == maskId) {
            maskId = store.newMaskId();
            segments.put(segment, maskId);
        }
        store.add(maskId, priority);
    }

    @Override
    public ClassificationNode<T, MaskType> freeze() {
        return new OptimisedGeneralEqualityNode<>(store, segments);
    }

    @Override
    public void link(Map<Operation, MutableNode<T, MaskType>> nodes) {
        var inequalityNode = nodes.get(NE);
        if (inequalityNode instanceof InequalityNode) {
            var node = (InequalityNode<T, MaskType>) inequalityNode;
            for (var x : segments.object2IntEntrySet()) {
                node.remove(x.getKey(), store.getMask(x.getIntValue()));
            }
        }
    }

    private static class OptimisedGeneralEqualityNode<Input, MaskType extends Mask<MaskType>>
            implements ClassificationNode<Input, MaskType> {
        private final Object2IntMap<Input> segments;
        private final MaskStore<MaskType> store;


        private OptimisedGeneralEqualityNode(MaskStore<MaskType> store, Object2IntMap<Input> segments) {
            this.segments = segments;
            this.store = store;
        }

        @Override
        public int match(Input input) {
            return segments.getOrDefault(input, 0);
        }

        @Override
        public double averageSelectivity() {
            return store.averageSelectivity(segments.values().toIntArray());
        }
    }
}
