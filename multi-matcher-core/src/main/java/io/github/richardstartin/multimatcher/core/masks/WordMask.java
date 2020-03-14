package io.github.richardstartin.multimatcher.core.masks;

import io.github.richardstartin.multimatcher.core.Mask;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class WordMask implements Mask<WordMask> {

  public static final MaskFactory<WordMask> FACTORY = new Factory();

  public static final int MAX_CAPACITY = 64;

  private long mask;

  public WordMask(long mask) {
    this.mask = mask;
  }

  public WordMask() { }

  public void add(int bit) {
    mask |= 1L << bit;
  }

  @Override
  public void remove(int id) {
    mask ^= (1L << id);
  }

  @Override
  public WordMask inPlaceAndNot(WordMask other) {
    this.mask &= ~other.mask;
    return this;
  }

  public WordMask inPlaceAnd(WordMask other) {
    this.mask &= other.mask;
    return this;
  }

  public WordMask or(WordMask other) {
    return new WordMask(this.mask | other.mask);
  }

  public WordMask inPlaceOr(WordMask other) {
    this.mask |= other.mask;
    return this;
  }

  @Override
  public WordMask resetTo(Mask<WordMask> other) {
    this.mask = other.unwrap().mask;
    return this;
  }

  @Override
  public void clear() {
    this.mask = 0;
  }

  @Override
  public WordMask unwrap() {
    return this;
  }

  @Override
  public IntStream stream() {
    return LongStream.iterate(mask, mask -> mask & (mask - 1))
                     .limit(Long.bitCount(mask))
                     .mapToInt(Long::numberOfTrailingZeros);
  }

  @Override
  public void forEach(IntConsumer consumer) {
    long word = mask;
    while (word != 0) {
      consumer.accept(Long.numberOfTrailingZeros(word));
      word &= (word - 1);
    }
  }

  @Override
  public int first() {
    if (!isEmpty()) {
      return Long.numberOfTrailingZeros(mask);
    }
    throw new NoSuchElementException("empty mask");
  }

  @Override
  public WordMask clone() {
    return new WordMask(mask);
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
    WordMask wordMask = (WordMask) o;
    return mask == wordMask.mask;
  }

  @Override
  public int hashCode() {
    return Objects.hash(mask);
  }

  private static final class Factory implements MaskFactory<WordMask> {
    private final WordMask EMPTY = newMask();
    @Override
    public WordMask newMask() {
      return new WordMask(0L);
    }

    @Override
    public WordMask contiguous(int max) {
      return new WordMask(((1L << max) - 1));
    }

    @Override
    public WordMask of(int... values) {
      long word = 0L;
      for (int v : values) {
        word |= (1L << v);
      }
      return new WordMask(word);
    }

    @Override
    public WordMask emptySingleton() {
      return EMPTY;
    }
  }
}
