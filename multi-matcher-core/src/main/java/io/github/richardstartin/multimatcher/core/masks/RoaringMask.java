package io.github.richardstartin.multimatcher.core.masks;

import io.github.richardstartin.multimatcher.core.Mask;
import org.roaringbitmap.IntIterator;
import org.roaringbitmap.RoaringBitmap;

import java.util.Objects;
import java.util.stream.IntStream;

public class RoaringMask implements Mask<RoaringMask> {

  public static MaskFactory<RoaringMask> FACTORY = new Factory();

  private final RoaringBitmap bitmap;

  private RoaringMask(RoaringBitmap bitmap) {
    this.bitmap = bitmap;
  }

  public RoaringMask() {
    this(new RoaringBitmap());
  }

  @Override
  public void add(int id) {
    bitmap.add(id);
  }

  @Override
  public void remove(int id) {
    bitmap.remove(id);
  }

  @Override
  public RoaringMask inPlaceAndNot(RoaringMask other) {
    bitmap.andNot(other.bitmap);
    return this;
  }

  @Override
  public RoaringMask inPlaceAnd(RoaringMask other) {
    if (other.isEmpty()) {
      return FACTORY.empty();
    }
    bitmap.and(other.bitmap);
    return this;
  }

  @Override
  public RoaringMask inPlaceOr(RoaringMask other) {
    if (other.isEmpty()) {
      return this;
    }
    bitmap.or(other.bitmap);
    return this;
  }

  @Override
  public RoaringMask resetTo(Mask<RoaringMask> other) {
    return inPlaceOr(other.unwrap());
  }

  @Override
  public void clear() {
    bitmap.clear();
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
  public int first() {
    return bitmap.first();
  }

  @Override
  public RoaringMask clone() {
    return new RoaringMask(bitmap.clone());
  }

  @Override
  public void optimise() {
    bitmap.runOptimize();
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
    private final RoaringMask EMPTY = empty();

    @Override
    public RoaringMask empty() {
      return new RoaringMask();
    }

    @Override
    public RoaringMask contiguous(int max) {
      RoaringBitmap range = new RoaringBitmap();
      range.add(0L, max & 0xFFFFFFFFL);
      return new RoaringMask(range);
    }

    @Override
    public RoaringMask of(int... values) {
      return new RoaringMask(RoaringBitmap.bitmapOf(values));
    }

    @Override
    public RoaringMask emptySingleton() {
      return EMPTY;
    }
  }
}
