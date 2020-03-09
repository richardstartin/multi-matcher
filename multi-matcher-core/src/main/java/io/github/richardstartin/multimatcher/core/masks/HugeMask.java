package io.github.richardstartin.multimatcher.core.masks;

import io.github.richardstartin.multimatcher.core.Mask;
import org.roaringbitmap.IntIterator;
import org.roaringbitmap.RoaringBitmap;

import java.util.Objects;
import java.util.stream.IntStream;

public class HugeMask implements Mask<HugeMask> {

  public static MaskFactory<HugeMask> FACTORY = new Factory();

  private final RoaringBitmap bitmap;

  private HugeMask(RoaringBitmap bitmap) {
    this.bitmap = bitmap;
  }

  public HugeMask() {
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
  public HugeMask and(HugeMask other) {
    if (other.isEmpty()) {
      return FACTORY.empty();
    }
    return new HugeMask(RoaringBitmap.and(bitmap, other.bitmap));
  }

  @Override
  public HugeMask andNot(HugeMask other) {
    return new HugeMask(RoaringBitmap.andNot(bitmap, other.bitmap));
  }

  @Override
  public HugeMask inPlaceAndNot(HugeMask other) {
    bitmap.andNot(other.bitmap);
    return this;
  }

  @Override
  public HugeMask or(HugeMask other) {
    if (other.isEmpty()) {
      return this;
    }
    return new HugeMask(RoaringBitmap.or(bitmap, other.bitmap));
  }

  @Override
  public HugeMask orNot(HugeMask other, int max) {
    RoaringBitmap not = other.bitmap.clone();
    not.flip(0, max & 0xFFFFFFFFL);
    return new HugeMask(RoaringBitmap.or(bitmap, not));
  }

  @Override
  public HugeMask inPlaceAnd(HugeMask other) {
    if (other.isEmpty()) {
      return FACTORY.empty();
    }
    bitmap.and(other.bitmap);
    return this;
  }

  @Override
  public HugeMask inPlaceOr(HugeMask other) {
    if (other.isEmpty()) {
      return this;
    }
    bitmap.or(other.bitmap);
    return this;
  }

  @Override
  public HugeMask resetTo(Mask<HugeMask> other) {
    return inPlaceOr(other.unwrap());
  }

  @Override
  public HugeMask unwrap() {
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
  public HugeMask clone() {
    return new HugeMask(bitmap.clone());
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
    HugeMask that = (HugeMask) o;
    return Objects.equals(bitmap, that.bitmap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bitmap);
  }

  private static final class Factory implements MaskFactory<HugeMask> {
    private final HugeMask EMPTY = empty();

    @Override
    public HugeMask empty() {
      return new HugeMask();
    }

    @Override
    public HugeMask contiguous(int max) {
      RoaringBitmap range = new RoaringBitmap();
      range.add(0L, max & 0xFFFFFFFFL);
      return new HugeMask(range);
    }

    @Override
    public HugeMask of(int... values) {
      return new HugeMask(RoaringBitmap.bitmapOf(values));
    }

    @Override
    public HugeMask emptySingleton() {
      return EMPTY;
    }
  }
}
