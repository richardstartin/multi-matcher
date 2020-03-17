package io.github.richardstartin.multimatcher.core.matchers.nodes;

import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.Operation;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;

import java.util.Arrays;

import static io.github.richardstartin.multimatcher.core.matchers.SelectivityHeuristics.avgCardinality;

public class IntNode<MaskType extends Mask<MaskType>> {

    private final Operation relation;
    private final MaskStore<MaskType> store;

    private int[] thresholds = new int[4];
    private int[] sets;
    private int count = 0;

    public IntNode(MaskStore<MaskType> store, Operation relation) {
        this.relation = relation;
        this.store = store;
        this.sets = new int[4];
        Arrays.fill(sets, -1);
    }

    public void add(int value, int priority) {
        if (count > 0 && value > thresholds[count - 1]) {
            ensureCapacity();
            int position = count;
            int maskId = sets[position];
            if (-1 == maskId) {
                maskId = store.newMaskId();
            }
            store.add(maskId, priority);
            thresholds[position] = value;
            sets[position] = maskId;
            ++count;
        } else {
            int position = Arrays.binarySearch(thresholds, 0, count, value);
            int insertionPoint = -(position + 1);
            if (position < 0 && insertionPoint < count) {
                ensureCapacity();
                for (int i = count; i > insertionPoint; --i) {
                    sets[i] = sets[i - 1];
                    thresholds[i] = thresholds[i - 1];
                }
                int maskId = store.newMaskId();
                store.add(maskId, priority);
                sets[insertionPoint] = maskId;
                thresholds[insertionPoint] = value;
                ++count;
            } else if (position < 0) {
                ensureCapacity();
                int maskId = store.newMaskId();
                store.add(maskId, priority);
                sets[count] = maskId;
                thresholds[count] = value;
                ++count;
            } else {
                store.add(sets[position], priority);
            }
        }
    }

    public int match(int value, int defaultValue) {
        switch (relation) {
            case GT:
                return findRangeEncoded(value);
            case GE:
                return findRangeEncodedInclusive(value);
            case LT:
                return findReverseRangeEncoded(value);
            case LE:
                return findReverseRangeEncodedInclusive(value);
            case EQ:
                return findEqualityEncoded(value);
            default:
                return defaultValue;
        }
    }


    public double averageSelectivity() {
        return store.averageSelectivity(sets);
    }

    public IntNode<MaskType> optimise() {
        switch (relation) {
            case GE:
            case GT:
                rangeEncode();
                break;
            case LE:
            case LT:
                reverseRangeEncode();
                break;
            default:
        }
        trim();
        return this;
    }

    private int findEqualityEncoded(int value) {
        int index = Arrays.binarySearch(thresholds, 0, count, value);
        return index >= 0 ? sets[index] : -1;
    }

    private int findRangeEncoded(int value) {
        int pos = Arrays.binarySearch(thresholds, 0, count, value);
        int index = (pos >= 0 ? pos : -(pos + 1)) - 1;
        return index >= 0 && index < count ? sets[index] : -1;
    }

    private int findRangeEncodedInclusive(int value) {
        int pos = Arrays.binarySearch(thresholds, 0, count, value);
        int index = (pos >= 0 ? pos : -(pos + 1) - 1);
        return index >= 0 && index < count ? sets[index] : -1;
    }

    private int findReverseRangeEncoded(int value) {
        int pos = Arrays.binarySearch(thresholds, 0, count, value);
        int index = (pos >= 0 ? pos + 1 : -(pos + 1));
        return index >= 0 && index < count ? sets[index] : -1;
    }

    private int findReverseRangeEncodedInclusive(int value) {
        int pos = Arrays.binarySearch(thresholds, 0, count, value);
        int index = (pos >= 0 ? pos : -(pos + 1));
        return index < count ? sets[index] : -1;
    }

    private void reverseRangeEncode() {
        for (int i = count - 2; i >= 0; --i) {
            store.or(sets[i + 1], sets[i]);
            store.optimise(sets[i]);
        }
    }

    private void rangeEncode() {
        for (int i = 1; i < count; ++i) {
            store.or(sets[i - 1], sets[i]);
            store.optimise(sets[i]);
        }
    }

    private void trim() {
        sets = Arrays.copyOf(sets, count);
        thresholds = Arrays.copyOf(thresholds, count);
    }

    private void ensureCapacity() {
        int newCount = count + 1;
        if (newCount == thresholds.length) {
            sets = Arrays.copyOf(sets, newCount * 2);
            Arrays.fill(sets, newCount, sets.length, -1);
            thresholds = Arrays.copyOf(thresholds, newCount * 2);
        }
    }

    @Override
    public String toString() {
        return Nodes.toString(count, relation,
                Arrays.stream(thresholds).boxed().iterator(),
                Arrays.stream(sets).iterator());
    }
}
