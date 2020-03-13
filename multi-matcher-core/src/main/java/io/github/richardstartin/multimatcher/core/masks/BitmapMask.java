package io.github.richardstartin.multimatcher.core.masks;

import io.github.richardstartin.multimatcher.core.Mask;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

public class BitmapMask implements Mask<BitmapMask> {

    public static final int MAX_CAPACITY = 256 * 64;
    private static final int UNKNOWN_EMPTY = -2;
    private static final int KNOWN_EMPTY = -1;

    public static Factory factory(int max) {
        return new Factory(max);
    }

    private static final long[] EMPTY = new long[256];

    private final long[] bitset;
    private int firstNonEmptyWord = UNKNOWN_EMPTY;

    BitmapMask(int max) {
        this(new long[(max + 63) >>> 6], UNKNOWN_EMPTY);
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
        if (!other.isEmpty()) {
            this.firstNonEmptyWord = UNKNOWN_EMPTY;
            int start = Math.max(0, other.firstNonEmptyWord - 1);
            Arrays.fill(bitset, 0, start, 0L);
            for (int i = start; i < bitset.length; ++i) {
                bitset[i] &= other.bitset[i];
            }
        } else {
            Arrays.fill(bitset, 0);
            this.firstNonEmptyWord = KNOWN_EMPTY;
        }
        return this;
    }

    @Override
    public BitmapMask inPlaceAndNot(BitmapMask other) {
        if (!other.isEmpty()) {
            int start = Math.max(0, firstNonEmptyWord - 1);
            Arrays.fill(bitset, 0, start, 0L);
            for (int i = 0; i < bitset.length; ++i) {
                bitset[i] &= ~other.bitset[i];
            }
            this.firstNonEmptyWord = UNKNOWN_EMPTY;
        }
        return this;
    }

    @Override
    public BitmapMask inPlaceOr(BitmapMask other) {
        if (!other.isEmpty()) {
            int start = Math.max(0, other.firstNonEmptyWord - 1);
            for (int i = start; i < bitset.length; ++i) {
                bitset[i] |= other.bitset[i];
            }
            this.firstNonEmptyWord = Math.min(firstNonEmptyWord, other.firstNonEmptyWord);
        }
        return this;
    }

    @Override
    public BitmapMask resetTo(Mask<BitmapMask> other) {
        if (other.isEmpty()) {
            this.firstNonEmptyWord = KNOWN_EMPTY;
        } else {
            System.arraycopy(other.unwrap().bitset, 0, bitset, 0, bitset.length);
            this.firstNonEmptyWord = other.unwrap().firstNonEmptyWord;
        }
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
        return isEmpty()
                ? -1
                : firstNonEmptyWord * Long.SIZE + Long.numberOfTrailingZeros(bitset[firstNonEmptyWord]);
    }

    @Override
    public BitmapMask clone() {
        return null == bitset
                ? this
                : new BitmapMask(Arrays.copyOf(bitset, bitset.length), firstNonEmptyWord);
    }

    @Override
    public void optimise() {
        firstNonEmptyWord = indexOfFirstNonEmptyWord(bitset);
    }

    @Override
    public boolean isEmpty() {
        if (firstNonEmptyWord == UNKNOWN_EMPTY) {
            firstNonEmptyWord = indexOfFirstNonEmptyWord(bitset);
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
        return Arrays.mismatch(bitset, 0, bitset.length, EMPTY, 0, bitset.length);
    }

    public static final class Factory implements MaskFactory<BitmapMask> {

        private final int max;
        private final BitmapMask empty;

        private Factory(int max) {
            this.max = max;
            this.empty = new BitmapMask(null, KNOWN_EMPTY);
        }

        @Override
        public BitmapMask newMask() {
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
