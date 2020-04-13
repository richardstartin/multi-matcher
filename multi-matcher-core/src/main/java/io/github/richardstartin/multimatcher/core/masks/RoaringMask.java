package io.github.richardstartin.multimatcher.core.masks;

import io.github.richardstartin.multimatcher.core.Mask;
import org.roaringbitmap.IntIterator;
import org.roaringbitmap.buffer.ImmutableRoaringBitmap;
import org.roaringbitmap.buffer.MutableRoaringBitmap;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

public class RoaringMask implements Mask<RoaringMask> {

    private final OptimisedStorage storage;
    private ImmutableRoaringBitmap bitmap;
    private RoaringMask(OptimisedStorage storage, MutableRoaringBitmap bitmap) {
        this.storage = storage;
        this.bitmap = bitmap;
    }

    public RoaringMask(OptimisedStorage storage) {
        this(storage, new MutableRoaringBitmap());
    }

    public static MaskStore<RoaringMask> store(int maxBufferSize, boolean direct) {
        return new Store(maxBufferSize, direct);
    }

    @Override
    public void add(int id) {
        ((MutableRoaringBitmap) bitmap).add(id);
    }

    @Override
    public void remove(int id) {
        ((MutableRoaringBitmap) bitmap).remove(id);
    }

    @Override
    public RoaringMask inPlaceAndNot(RoaringMask other) {
        ((MutableRoaringBitmap) bitmap).andNot(other.bitmap);
        return this;
    }

    @Override
    public RoaringMask inPlaceAnd(RoaringMask other) {
        ((MutableRoaringBitmap) bitmap).and(other.bitmap);
        return this;
    }

    @Override
    public RoaringMask inPlaceOr(RoaringMask other) {
        ((MutableRoaringBitmap) bitmap).or(other.bitmap);
        return this;
    }

    @Override
    public RoaringMask inPlaceNot(int max) {
        ((MutableRoaringBitmap) bitmap).flip(0L, max);
        return this;
    }

    @Override
    public RoaringMask resetTo(Mask<RoaringMask> other) {
        this.bitmap = other.unwrap().bitmap.toMutableRoaringBitmap();
        return this;
    }

    @Override
    public void clear() {
        ((MutableRoaringBitmap) bitmap).clear();
    }

    @Override
    public RoaringMask unwrap() {
        return this;
    }

    @Override
    public IntStream stream() {
        IntIterator it = bitmap.getIntIterator();
        return IntStream.range(0, bitmap.getCardinality())
                .map(i -> it.next());
    }

    @Override
    public void forEach(IntConsumer consumer) {
        bitmap.forEach((org.roaringbitmap.IntConsumer) consumer::accept);
    }

    @Override
    public int first() {
        return bitmap.first();
    }

    @Override
    public RoaringMask clone() {
        return new RoaringMask(storage, bitmap.toMutableRoaringBitmap());
    }

    @Override
    public void optimise() {
        ((MutableRoaringBitmap) bitmap).trim();
        ((MutableRoaringBitmap) bitmap).runOptimize();
        this.bitmap = storage.consolidate(((MutableRoaringBitmap) bitmap));
    }

    @Override
    public boolean isEmpty() {
        return bitmap.isEmpty();
    }

    @Override
    public int cardinality() {
        return bitmap.getCardinality();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoaringMask that = (RoaringMask) o;
        return Objects.equals(bitmap, that.bitmap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bitmap);
    }

    private static final class Store implements MaskStore<RoaringMask> {
        private final OptimisedStorage storage;

        private final ThreadLocal<RoaringMask> temp;

        private RoaringMask[] bitmaps = new RoaringMask[4];
        private int maskId = 0;

        private Store(int bufferSize, boolean direct) {
            this.storage = new OptimisedStorage(direct
                    ? ByteBuffer.allocateDirect(bufferSize)
                    : ByteBuffer.allocate(bufferSize));
            temp = ThreadLocal.withInitial(this::newMask);
            bitmaps[0] = newMask();
        }

        @Override
        public RoaringMask newMask() {
            return new RoaringMask(storage);
        }

        @Override
        public int newMaskId() {
            ensureCapacity(++maskId);
            bitmaps[maskId] = new RoaringMask(storage);
            return maskId;
        }

        @Override
        public int newMaskId(int copyAddress) {
            ensureCapacity(++maskId);
            bitmaps[maskId] = bitmaps[copyAddress].clone();
            return maskId;
        }

        @Override
        public int storeMask(RoaringMask mask) {
            ensureCapacity(++maskId);
            bitmaps[maskId] = mask;
            return maskId;
        }

        @Override
        public RoaringMask getMask(int id) {
            return bitmaps[id & (bitmaps.length - 1)];
        }

        @Override
        public void add(int id, int bit) {
            bitmaps[id & (bitmaps.length - 1)].add(bit);
        }

        @Override
        public void remove(int id, int bit) {
            bitmaps[id & (bitmaps.length - 1)].remove(bit);
        }

        @Override
        public void or(int from, int into) {
            bitmaps[into & (bitmaps.length - 1)].inPlaceOr(bitmaps[from & (bitmaps.length - 1)]);
        }

        @Override
        public void andNot(int from, int into) {
            bitmaps[into & (bitmaps.length - 1)].inPlaceAndNot(bitmaps[from & (bitmaps.length - 1)]);
        }

        @Override
        public void optimise(int id) {
            bitmaps[id & (bitmaps.length - 1)].optimise();
        }

        @Override
        public RoaringMask getTemp() {
            return temp.get();
        }

        @Override
        public RoaringMask getTemp(int copyAddress) {
            return temp.get().resetTo(bitmaps[copyAddress & (bitmaps.length - 1)]);
        }

        @Override
        public void orInto(RoaringMask mask, int id) {
            mask.inPlaceOr(bitmaps[id & (bitmaps.length - 1)]);
        }

        @Override
        public void andInto(RoaringMask mask, int id) {
            mask.inPlaceAnd(bitmaps[id & (bitmaps.length - 1)]);
        }

        @Override
        public RoaringMask contiguous(int max) {
            MutableRoaringBitmap range = new MutableRoaringBitmap();
            range.add(0L, max & 0xFFFFFFFFL);
            return new RoaringMask(storage, range);
        }

        @Override
        public int newContiguousMaskId(int max) {
            ensureCapacity(++maskId);
            bitmaps[maskId] = contiguous(max);
            return maskId;
        }

        @Override
        public boolean isEmpty(int id) {
            return bitmaps[id & (bitmaps.length - 1)].isEmpty();
        }

        @Override
        public RoaringMask of(int... values) {
            return new RoaringMask(storage, MutableRoaringBitmap.bitmapOf(values));
        }

        @Override
        public double averageSelectivity(int[] ids, int min, int max) {
            double cardinality = 0;
            for (int i = min; i < max; ++i) {
                cardinality += this.bitmaps[ids[i]].cardinality();
            }
            return cardinality / bitmaps.length;
        }

        private void ensureCapacity(int maskId) {
            if (maskId >= bitmaps.length) {
                bitmaps = Arrays.copyOf(bitmaps, bitmaps.length * 2);
            }
        }
    }

    private static class OptimisedStorage {
        private final ByteBuffer allocatedSpace;

        private OptimisedStorage(ByteBuffer allocatedSpace) {
            this.allocatedSpace = allocatedSpace;
        }

        ImmutableRoaringBitmap consolidate(MutableRoaringBitmap bitmap) {
            int requiredSize = bitmap.serializedSizeInBytes();
            if (allocatedSpace.remaining() < requiredSize) {
                // can't consolidate
                return bitmap;
            } else {
                int pos = allocatedSpace.position();
                bitmap.serialize(allocatedSpace);
                allocatedSpace.position(pos);
                var consolidated = new ImmutableRoaringBitmap(allocatedSpace);
                allocatedSpace.position(pos + requiredSize);
                return consolidated;
            }
        }
    }
}
