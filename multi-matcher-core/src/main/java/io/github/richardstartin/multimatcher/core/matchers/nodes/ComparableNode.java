package io.github.richardstartin.multimatcher.core.matchers.nodes;

import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.Operation;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import io.github.richardstartin.multimatcher.core.matchers.ClassificationNode;
import io.github.richardstartin.multimatcher.core.matchers.MutableNode;

import java.util.Comparator;
import java.util.NavigableMap;
import java.util.TreeMap;

import static io.github.richardstartin.multimatcher.core.matchers.SelectivityHeuristics.avgCardinality;

public class ComparableNode<T, MaskType extends Mask<MaskType>>
        implements MutableNode<T, MaskType>, ClassificationNode<T, MaskType> {

    private final MaskStore<MaskType> store;
    private final NavigableMap<T, Integer> sets;
    private final Operation operation;

    public ComparableNode(MaskStore<MaskType> store,
                          Comparator<T> comparator,
                          Operation operation) {
        this.sets = new TreeMap<>(comparator);
        this.operation = operation;
        this.store = store;
    }

    public void add(T value, int priority) {
        int set = sets.getOrDefault(value, 0);
        if (0 == set) {
            set = store.newMaskId();
            sets.put(value, set);
        }
        store.add(set, priority);
    }

    @Override
    public int match(T value) {
        switch (operation) {
            case GE:
            case EQ:
            case LE:
                return sets.getOrDefault(value, 0);
            case LT:
                var higher = sets.higherEntry(value);
                return null == higher ? 0 : higher.getValue();
            case GT:
                var lower = sets.lowerEntry(value);
                return null == lower ? 0 : lower.getValue();
            default:
                return 0;
        }
    }

    public ComparableNode<T, MaskType> freeze() {
        switch (operation) {
            case GE:
            case GT:
                rangeEncode();
                return this;
            case LE:
            case LT:
                reverseRangeEncode();
                return this;
            default:
                return this;
        }
    }

    public double averageSelectivity() {
        return store.averageSelectivity(sets.values().stream().mapToInt(Integer::intValue).toArray());
    }

    private void rangeEncode() {
        int prev = 0;
        for (var set : sets.entrySet()) {
            int next = set.getValue();
            store.or(prev, next);
            store.optimise(next);
            prev = set.getValue();
        }
    }

    private void reverseRangeEncode() {
        int prev = 0;
        for (var set : sets.descendingMap().entrySet()) {
            int next = set.getValue();
            store.or(prev, next);
            store.optimise(next);
            prev = set.getValue();
        }
    }

    @Override
    public String toString() {
        return Nodes.toString(sets.size(), operation, sets);
    }
}
