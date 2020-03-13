package io.github.richardstartin.multimatcher.core.masks;

import io.github.richardstartin.multimatcher.core.Mask;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class TinyMask implements Mask<TinyMask> {

  public static final MaskFactory<TinyMask> FACTORY = new Factory();

  public static final int MAX_CAPACITY = 64;

  private long mask;

  public TinyMask(long mask) {
    this.mask = mask;
  }

  public TinyMask() { }

  public void add(int bit) {
    mask |= 1L << bit;
  }

  @Override
  public void remove(int id) {
    mask ^= (1L << id);
  }

  @Override
  public TinyMask inPlaceAndNot(TinyMask other) {
    this.mask &= ~other.mask;
    return this;
  }

  public TinyMask inPlaceAnd(TinyMask other) {
    this.mask &= other.mask;
    return this;
  }

  public TinyMask or(TinyMask other) {
    return new TinyMask(this.mask | other.mask);
  }

  public TinyMask inPlaceOr(TinyMask other) {
    this.mask |= other.mask;
    return this;
  }

  @Override
  public TinyMask resetTo(Mask<TinyMask> other) {
    this.mask = other.unwrap().mask;
    return this;
  }

  @Override
  public void clear() {
    this.mask = 0;
  }

  @Override
  public TinyMask unwrap() {
    return this;
  }

  @Override
  public IntStream stream() {
    return LongStream.iterate(mask, mask -> mask & (mask - 1))
                     .limit(Long.bitCount(mask))
                     .mapToInt(Long::numberOfTrailingZeros);
  }

  @Override
  public int first() {
    if (!isEmpty()) {
      return Long.numberOfTrailingZeros(mask);
    }
    throw new NoSuchElementException("empty mask");
  }

  @Override
  public TinyMask clone() {
    return new TinyMask(mask);
  }

  @Override
  public void optimise() {

  }

  @Override
  public boolean isEmpty() {
    return mask == 0L;
  }

  @Override
  public int cardinality() {
    return Long.bitCount(mask);
  }

  @Override
  public String toString() {
    return Long.toBinaryString(mask);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TinyMask tinyMask = (TinyMask) o;
    return mask == tinyMask.mask;
  }

  @Override
  public int hashCode() {
    return Objects.hash(mask);
  }

  private static final class Factory implements MaskFactory<TinyMask> {
    private final TinyMask EMPTY = newMask();
    @Override
    public TinyMask newMask() {
      return new TinyMask(0L);
    }

    @Override
    public TinyMask contiguous(int max) {
      return new TinyMask(((1L << max) - 1));
    }

    @Override
    public TinyMask of(int... values) {
      long word = 0L;
      for (int v : values) {
        word |= (1L << v);
      }
      return new TinyMask(word);
    }

    @Override
    public TinyMask emptySingleton() {
      return EMPTY;
    }
  }
}
