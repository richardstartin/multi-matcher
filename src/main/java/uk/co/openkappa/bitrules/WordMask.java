package uk.co.openkappa.bitrules;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class WordMask implements Mask<WordMask> {

  public static final int MAX_CAPACITY = 64;

  public static WordMask contiguous(int to) {
    return new WordMask(((1L << to) - 1));
  }

  public static WordMask of(int... values) {
    long word = 0L;
    for (int v : values) {
      word |= (1L << v);
    }
    return new WordMask(word);
  }

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

  public WordMask and(WordMask other) {
    return new WordMask(this.mask & other.mask);
  }

  @Override
  public WordMask andNot(WordMask other) {
    return new WordMask(mask &~ other.mask);
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
  public String toString() {
    return Long.toBinaryString(mask);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    WordMask tinyMask = (WordMask) o;
    return mask == tinyMask.mask;
  }

  @Override
  public int hashCode() {
    return Objects.hash(mask);
  }
}
