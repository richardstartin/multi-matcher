package uk.co.openkappa.bitrules;

import org.roaringbitmap.IntIterator;
import org.roaringbitmap.RoaringBitmap;

import java.util.Objects;
import java.util.stream.IntStream;

public class RoaringBitmapMask implements Mask<RoaringBitmapMask> {

  public static RoaringBitmapMask contiguous(int to) {
    RoaringBitmap range = new RoaringBitmap();
    range.add(0L, to & 0xFFFFFFFFL);
    return new RoaringBitmapMask(range);
  }

  private final RoaringBitmap bitmap;

  private RoaringBitmapMask(RoaringBitmap bitmap) {
    this.bitmap = bitmap;
  }

  public RoaringBitmapMask() {
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
  public RoaringBitmapMask and(RoaringBitmapMask other) {
    return new RoaringBitmapMask(RoaringBitmap.and(bitmap, other.bitmap));
  }

  @Override
  public RoaringBitmapMask andNot(RoaringBitmapMask other) {
    return new RoaringBitmapMask(RoaringBitmap.andNot(bitmap, other.bitmap));
  }

  @Override
  public RoaringBitmapMask or(RoaringBitmapMask other) {
    return new RoaringBitmapMask(RoaringBitmap.or(bitmap, other.bitmap));
  }

  @Override
  public RoaringBitmapMask inPlaceAnd(RoaringBitmapMask other) {
    bitmap.and(other.bitmap);
    return this;
  }

  @Override
  public RoaringBitmapMask inPlaceOr(RoaringBitmapMask other) {
    bitmap.or(other.bitmap);
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
  public RoaringBitmapMask clone() {
    return new RoaringBitmapMask(bitmap.clone());
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RoaringBitmapMask that = (RoaringBitmapMask) o;
    return Objects.equals(bitmap, that.bitmap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bitmap);
  }
}
