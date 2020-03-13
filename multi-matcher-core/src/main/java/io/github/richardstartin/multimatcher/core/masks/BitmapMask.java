package io.github.richardstartin.multimatcher.core.masks;

import io.github.richardstartin.multimatcher.core.Mask;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

public class BitmapMask implements Mask<BitmapMask> {

    public static final int MAX_CAPACITY = 256 * 64;
    private static final int UNKNOWN_FIRST_NON_EMPTY = -1;
    private static final int KNOWN_EMPTY = -2;

    public static Factory factory(int max) {
        return new Factory(max);
    }

    private static final long[] EMPTY = new long[128];

    private final long[] bitset;
    private int firstNonEmptyWord = UNKNOWN_FIRST_NON_EMPTY;

    BitmapMask(int max) {
        this(new long[(max + 63) >>> 6], UNKNOWN_FIRST_NON_EMPTY);
    }

    BitmapMask(int max, int from, int to) {
        this.bitset = new long[(max + 63) >>> 6];
        Arrays.fill(bitset, (from + 63) >>> 6, to >>> 6, -1L);
        bitset[from >>> 6] |= ((1L << from) - 1);
        bitset[(to - 1) >>> 6] |= ((1L << to) - 1);
    }

    BitmapMask(long[] bitset, int firstNonEmptyWord) {
        this.bitset = bitset;
        this.firstNonEmptyWord = firstNonEmptyWord;
    }


    @Override
    public void add(int id) {
        bitset[id >>> 6] |= (1L << id);
    }

    @Override
    public void remove(int id) {
        bitset[id >>> 6] ^= (1L << id);
    }

    @Override
    public BitmapMask inPlaceAnd(BitmapMask other) {
        this.firstNonEmptyWord = UNKNOWN_FIRST_NON_EMPTY;
        int start = Math.max(0, other.firstNonEmptyWord - 1);
        Arrays.fill(bitset, 0, start, 0L);
        for (int i = start; i < bitset.length; ++i) {
            bitset[i] &= other.bitset[i];
        }
        return this;
    }

    @Override
    public BitmapMask inPlaceAndNot(BitmapMask other) {
        int start = Math.max(0, firstNonEmptyWord - 1);
        Arrays.fill(bitset, 0, start, 0L);
        for (int i = 0; i < bitset.length; ++i) {
            bitset[i] &= ~other.bitset[i];
        }
        this.firstNonEmptyWord = UNKNOWN_FIRST_NON_EMPTY;
        return this;
    }

    @Override
    public BitmapMask inPlaceOr(BitmapMask other) {
        int start = Math.max(0, other.firstNonEmptyWord - 1);
        for (int i = start; i < bitset.length; ++i) {
            bitset[i] |= other.bitset[i];
        }
        this.firstNonEmptyWord = Math.min(firstNonEmptyWord, other.firstNonEmptyWord);
        return this;
    }

    @Override
    public BitmapMask resetTo(Mask<BitmapMask> other) {
        System.arraycopy(other.unwrap().bitset, 0, bitset, 0, bitset.length);
        this.firstNonEmptyWord = other.unwrap().firstNonEmptyWord;
        return this;
    }

    @Override
    public void clear() {
        Arrays.fill(bitset, 0L);
    }

    @Override
    public BitmapMask unwrap() {
        return this;
    }

    @Override
    public IntStream stream() {
        return IntStream.range(0, bitset.length)
                .flatMap(i -> {
                    int[] bits = new int[Long.bitCount(bitset[i])];
                    long word = bitset[i];
                    int j = 0;
                    while (word != 0) {
                        bits[j++] = Long.numberOfTrailingZeros(word) + (i * Long.SIZE);
                        word &= (word - 1);
                    }
                    return Arrays.stream(bits);
                });
    }

    @Override
    public int first() {
        if (firstNonEmptyWord == UNKNOWN_FIRST_NON_EMPTY) {
            firstNonEmptyWord = indexOfFirstNonEmptyWord(bitset);
        }
        int wordIndex = firstNonEmptyWord;
        return wordIndex < 0
                ? -1
                : wordIndex * Long.SIZE + Long.numberOfTrailingZeros(bitset[wordIndex]);
    }

    @Override
    public BitmapMask clone() {
        return new BitmapMask(Arrays.copyOf(bitset, bitset.length), firstNonEmptyWord);
    }

    @Override
    public void optimise() {
        firstNonEmptyWord = indexOfFirstNonEmptyWord(bitset);
    }

    @Override
    public boolean isEmpty() {
        if (firstNonEmptyWord == UNKNOWN_FIRST_NON_EMPTY) {
            int first = indexOfFirstNonEmptyWord(bitset);
            firstNonEmptyWord = first == UNKNOWN_FIRST_NON_EMPTY ? KNOWN_EMPTY : first;
        }
        return firstNonEmptyWord == KNOWN_EMPTY;
    }

    @Override
    public int cardinality() {
        return computeCardinality();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BitmapMask that = (BitmapMask) o;
        return Arrays.equals(bitset, that.bitset);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(firstNonEmptyWord);
        result = 31 * result + Arrays.hashCode(bitset);
        return result;
    }

    private int computeCardinality() {
        if (isEmpty()) {
            return 0;
        }
        int cardinality = 0;
        for (int i = firstNonEmptyWord; i < bitset.length; ++i) {
            cardinality += Long.bitCount(bitset[i]);
            ++i;
        }
        return cardinality;
    }

    private static int indexOfFirstNonEmptyWord(long[] bitset) {
        int firstNonEmpty = UNKNOWN_FIRST_NON_EMPTY;
        int i = 0;
        for (; i + EMPTY.length - 1 < bitset.length && firstNonEmpty == UNKNOWN_FIRST_NON_EMPTY; i += EMPTY.length) {
            firstNonEmpty = Arrays.mismatch(bitset, i, i + EMPTY.length, EMPTY, 0, EMPTY.length);
        }
        for (; i < bitset.length && firstNonEmpty == UNKNOWN_FIRST_NON_EMPTY; ++i) {
            if (bitset[i] != 0) {
                firstNonEmpty = i;
            }
        }
        return firstNonEmpty;
    }

    public static final class Factory implements MaskFactory<BitmapMask> {

        private final int max;
        private final BitmapMask empty;

        private Factory(int max) {
            this.max = max;
            this.empty = new BitmapMask(max);
        }

        @Override
        public BitmapMask empty() {
            return new BitmapMask(max);
        }

        @Override
        public BitmapMask contiguous(int max) {
            if (max > this.max) {
                throw new IllegalArgumentException();
            }
            return new BitmapMask(this.max, 0, max);
        }

        @Override
        public BitmapMask of(int... values) {
            var mask = new BitmapMask(max);
            for (int value : values) {
                mask.add(value);
            }
            return mask;
        }

        @Override
        public BitmapMask emptySingleton() {
            return empty;
        }
    }
}
