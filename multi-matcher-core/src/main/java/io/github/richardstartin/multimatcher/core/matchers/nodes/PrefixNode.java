package io.github.richardstartin.multimatcher.core.matchers.nodes;

import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import io.github.richardstartin.multimatcher.core.matchers.ClassificationNode;
import io.github.richardstartin.multimatcher.core.matchers.MutableNode;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.util.Comparator;

import static io.github.richardstartin.multimatcher.core.matchers.SelectivityHeuristics.avgCardinality;

public class PrefixNode<MaskType extends Mask<MaskType>> implements MutableNode<String, MaskType>,
        ClassificationNode<String, MaskType> {

    private final MaskStore<MaskType> store;
    private final Object2IntOpenHashMap<String> map;
    private int longest;

    public PrefixNode(MaskStore<MaskType> store) {
        this(store, new Object2IntOpenHashMap<>(), 0);
    }

    private PrefixNode(MaskStore<MaskType> store, Object2IntOpenHashMap<String> map, int longest) {
        this.store = store;
        this.map = map;
        this.longest = longest;
    }

    @Override
    public int match(String value) {
        int position = longest;
        while (position > 0) {
            int match = map.getOrDefault(value.substring(0, position), 0);
            if (0 != match) {
                return match;
            }
            --position;
        }
        return 0;
    }

    @Override
    public ClassificationNode<String, MaskType> freeze() {
        this.longest = map.keySet().stream().mapToInt(String::length).max().orElse(0);
        PatriciaTrie<Integer> trie = new PatriciaTrie<>();
        trie.putAll(map);
        map.keySet().stream().sorted(Comparator.comparingInt(String::length))
                .forEach(key -> {
                    int mask = map.getInt(key);
                    trie.prefixMap(key)
                            .forEach((k, v) -> {
                                if (!key.equals(k)) {
                                    store.or(mask, v);
                                }
                            });
                });
        return new PrefixNode<>(store, map, longest);
    }

    public void add(String prefix, int id) {
        map.compute(prefix, (p, mask) -> {
            if (null == mask) {
                mask = store.newMaskId();
            }
            store.add(mask, id);
            return mask;
        });
    }

    @Override
    public double averageSelectivity() {
        // probably completely wrong
        return store.averageSelectivity(map.values().toIntArray());
    }
}
