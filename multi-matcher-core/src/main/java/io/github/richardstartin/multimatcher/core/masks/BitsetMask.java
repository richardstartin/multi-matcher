package io.github.richardstartin.multimatcher.core.masks;

import io.github.richardstartin.multimatcher.core.Mask;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

public class BitsetMask implements Mask<BitsetMask> {

    public static final int MAX_CAPACITY = 256 * 64;
    private static final int UNKNOWN_EMPTY = -2;
    private static final int KNOWN_EMPTY = -1;
    private static final long[] EMPTY = new long[256];

    private final long[] bitset;
    private int firstNonEmptyWord = UNKNOWN_EMPTY;
    BitsetMask(int max) {
        this(new long[(max + 63) >>> 6], UNKNOWN_EMPTY);
    }

    BitsetMask(int max, int from, int to) {
        this.bitset = new long[(max + 63) >>> 6];
        Arrays.fill(bitset, (from + 63) >>> 6, to >>> 6, -1L);
        bitset[from >>> 6] |= ((1L << from) - 1);
        bitset[(to - 1) >>> 6] |= ((1L << to) - 1);
    }

    BitsetMask(long[] bitset, int firstNonEmptyWord) {
        this.bitset = bitset;
        this.firstNonEmptyWord = firstNonEmptyWord;
    }

    public static MaskStore<BitsetMask> store(int max) {
        return new Store(max);
    }

    private static int indexOfFirstNonEmptyWord(long[] bitset) {
        return Arrays.mismatch(bitset, 0, bitset.length, EMPTY, 0, bitset.length);
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
    public BitsetMask inPlaceAnd(BitsetMask other) {
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
    public BitsetMask inPlaceAndNot(BitsetMask other) {
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
    public BitsetMask inPlaceOr(BitsetMask other) {
        if (!other.isEmpty()) {
            int start = Math.max(0, other.firstNonEmptyWord - 1);
            for (int i = start; i < bitset.length; ++i) {
                bitset[i] |= other.bitset[i];
            }
            this.firstNonEmptyWord = firstNonEmptyWord == KNOWN_EMPTY
                ? other.firstNonEmptyWord
                : Math.min(firstNonEmptyWord, other.firstNonEmptyWord);
        }
        return this;
    }

    @Override
    public BitsetMask resetTo(Mask<BitsetMask> other) {
        if (other.isEmpty()) {
            if (firstNonEmptyWord != KNOWN_EMPTY) {
                Arrays.fill(bitset, Math.max(firstNonEmptyWord, 0), bitset.length, 0L);
                this.firstNonEmptyWord = KNOWN_EMPTY;
            }
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
    public BitsetMask unwrap() {
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
    public void forEach(IntConsumer consumer) {
        if (!isEmpty()) {
            for (int wordIndex = firstNonEmptyWord; wordIndex < bitset.length; ++wordIndex) {
                long word = bitset[wordIndex];
                while (word != 0) {
                    consumer.accept(wordIndex * Long.SIZE + Long.numberOfTrailingZeros(word));
                    word &= (word - 1);
                }
            }
        }
    }

    @Override
    public int first() {
        return isEmpty()
                ? -1
                : firstNonEmptyWord * Long.SIZE + Long.numberOfTrailingZeros(bitset[firstNonEmptyWord]);
    }

    @Override
    public BitsetMask clone() {
        return null == bitset
                ? this
                : new BitsetMask(Arrays.copyOf(bitset, bitset.length), firstNonEmptyWord);
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
        BitsetMask that = (BitsetMask) o;
        return Arrays.equals(bitset, that.bitset);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(firstNonEmptyWord);
        result = 31 * result + Arrays.hashCode(bitset);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append(0)
                .append(':')
                .append(Long.toBinaryString(bitset[0]));
        for (int i = 1; i < bitset.length; ++i) {
            sb.append(',').append(i).append(':').append(Long.toBinaryString(bitset[i]));
        }
        return sb.toString();
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

    public static final class Store implements MaskStore<BitsetMask> {

        private static final BitsetMask EMPTY = new BitsetMask(null, KNOWN_EMPTY);

        private final int max;
        private final ThreadLocal<BitsetMask> temp;

        private BitsetMask[] bitsets = new BitsetMask[4];
        private int maskId = 0;

        private Store(int max) {
            this.max = max;
            this.temp = ThreadLocal.withInitial(this::newMask);
            bitsets[0] = EMPTY;
        }

        @Override
        public BitsetMask newMask() {
            return new BitsetMask(max);
        }

        @Override
        public int newMaskId() {
            ensureCapacity(++maskId);
            bitsets[maskId] = newMask();
            return maskId;
        }

        @Override
        public int newMaskId(int copyAddress) {
            ensureCapacity(++maskId);
            bitsets[maskId] = bitsets[copyAddress].clone();
            return maskId;
        }

        @Override
        public BitsetMask getMask(int id) {
            return bitsets[id & (bitsets.length - 1)];
        }

        @Override
        public void add(int id, int bit) {
            bitsets[id & (bitsets.length - 1)].add(bit);
        }

        @Override
        public void remove(int id, int bit) {
            if (id != 0) {
                bitsets[id & (bitsets.length - 1)].remove(bit);
            }
        }

        @Override
        public void or(int from, int into) {
            bitsets[into & (bitsets.length - 1)].inPlaceOr(bitsets[from & (bitsets.length - 1)]);
        }

        @Override
        public void andNot(int from, int into) {
            bitsets[into & (bitsets.length - 1)].inPlaceAndNot(bitsets[from & (bitsets.length - 1)]);
        }

        @Override
        public void optimise(int id) {
            bitsets[id & (bitsets.length - 1)].optimise();
        }

        @Override
        public BitsetMask getTemp() {
            return temp.get();
        }

        @Override
        public BitsetMask getTemp(int copyAddress) {
            return temp.get().resetTo(bitsets[copyAddress & (bitsets.length - 1)]);
        }

        @Override
        public void orInto(BitsetMask mask, int id) {
            mask.inPlaceOr(bitsets[id & (bitsets.length - 1)]);
        }

        @Override
        public void andInto(BitsetMask mask, int id) {
            mask.inPlaceAnd(bitsets[id & (bitsets.length - 1)]);
        }

        @Override
        public BitsetMask contiguous(int max) {
            if (max > this.max) {
                throw new IllegalArgumentException();
            }
            return new BitsetMask(this.max, 0, max);
        }

        @Override
        public int newContiguousMaskId(int max) {
            ensureCapacity(++maskId);
            bitsets[maskId] = contiguous(max);
            return maskId;
        }

        @Override
        public boolean isEmpty(int id) {
            return bitsets[id & (bitsets.length - 1)].isEmpty();
        }

        @Override
        public BitsetMask of(int... values) {
            var mask = new BitsetMask(max);
            for (int value : values) {
                mask.add(value);
            }
            return mask;
        }

        @Override
        public double averageSelectivity(int[] ids, int min, int max) {
            double selectivity = 0f;
            for (int i = min; i < max; ++i) {
                selectivity += bitsets[ids[i]].cardinality();
            }
            return selectivity / ids.length;
        }

        private void ensureCapacity(int maskId) {
            if (maskId >= bitsets.length) {
                bitsets = Arrays.copyOf(bitsets, bitsets.length * 2);
            }
        }
    }
}
