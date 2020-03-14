package io.github.richardstartin.multimatcher.core.masks;

import io.github.richardstartin.multimatcher.core.Mask;
import org.roaringbitmap.IntIterator;
import org.roaringbitmap.buffer.ImmutableRoaringBitmap;
import org.roaringbitmap.buffer.MutableRoaringBitmap;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

public class RoaringMask implements Mask<RoaringMask> {

  public static MaskFactory<RoaringMask> factory(int maxBufferSize, boolean direct) {
    return new Factory(maxBufferSize, direct);
  }

  private final OptimisedStorage storage;
  private ImmutableRoaringBitmap bitmap;

  private RoaringMask(OptimisedStorage storage, MutableRoaringBitmap bitmap) {
    this.storage = storage;
    this.bitmap = bitmap;
  }

  public RoaringMask(OptimisedStorage storage) {
    this(storage, new MutableRoaringBitmap());
  }

  @Override
  public void add(int id) {
    ((MutableRoaringBitmap)bitmap).add(id);
  }

  @Override
  public void remove(int id) {
    ((MutableRoaringBitmap)bitmap).remove(id);
  }

  @Override
  public RoaringMask inPlaceAndNot(RoaringMask other) {
    ((MutableRoaringBitmap)bitmap).andNot(other.bitmap);
    return this;
  }

  @Override
  public RoaringMask inPlaceAnd(RoaringMask other) {
    ((MutableRoaringBitmap)bitmap).and(other.bitmap);
    return this;
  }

  @Override
  public RoaringMask inPlaceOr(RoaringMask other) {
    ((MutableRoaringBitmap)bitmap).or(other.bitmap);
    return this;
  }

  @Override
  public RoaringMask resetTo(Mask<RoaringMask> other) {
    this.bitmap = other.unwrap().bitmap.toMutableRoaringBitmap();
    return this;
  }

  @Override
  public void clear() {
    ((MutableRoaringBitmap)bitmap).clear();
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
    ((MutableRoaringBitmap)bitmap).trim();
    ((MutableRoaringBitmap)bitmap).runOptimize();
    this.bitmap = storage.consolidate(((MutableRoaringBitmap)bitmap));
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

  private static final class Factory implements MaskFactory<RoaringMask> {
    private final RoaringMask empty = newMask();

    private final OptimisedStorage storage;

    private Factory(int bufferSize, boolean direct) {
      this.storage = new OptimisedStorage(direct
              ? ByteBuffer.allocateDirect(bufferSize)
              : ByteBuffer.allocate(bufferSize));
    }

    @Override
    public RoaringMask newMask() {
      return new RoaringMask(storage);
    }

    @Override
    public RoaringMask contiguous(int max) {
      MutableRoaringBitmap range = new MutableRoaringBitmap();
      range.add(0L, max & 0xFFFFFFFFL);
      return new RoaringMask(storage, range);
    }

    @Override
    public RoaringMask of(int... values) {
      return new RoaringMask(storage, MutableRoaringBitmap.bitmapOf(values));
    }

    @Override
    public RoaringMask emptySingleton() {
      return empty;
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
