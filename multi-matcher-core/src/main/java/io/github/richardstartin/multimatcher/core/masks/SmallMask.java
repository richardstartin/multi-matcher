package io.github.richardstartin.multimatcher.core.masks;

import io.github.richardstartin.multimatcher.core.Mask;
import org.roaringbitmap.*;

import java.util.stream.IntStream;


public class SmallMask implements Mask<SmallMask> {

  public static final MaskFactory<SmallMask> FACTORY = new Factory();

  public static final int MAX_CAPACITY = 1 << 16;

  private Container container;

  public SmallMask(Container container) {
    this.container = container;
  }

  public SmallMask() {
    this(new ArrayContainer());
  }

  @Override
  public void add(int id) {
    container = container.add((short) id);
  }

  @Override
  public void remove(int id) {
    container = container.remove((short) id);
  }

  @Override
  public SmallMask and(SmallMask other) {
    if (other.isEmpty()) {
      return FACTORY.empty();
    }
    return new SmallMask(container.and(other.container));
  }

  @Override
  public SmallMask andNot(SmallMask other) {
    return new SmallMask(container.andNot(other.container));
  }

  @Override
  public SmallMask inPlaceAndNot(SmallMask other) {
    var andNot = this.container.andNot(other.container);
    if (container != andNot) {
      this.container = andNot;
    }
    return this;
  }

  @Override
  public SmallMask inPlaceAnd(SmallMask other) {
    if (other.isEmpty()) {
      return FACTORY.empty();
    }
    var and = container.iand(other.container);
    if (and != container) {
      this.container = and;
    }
    return this;
  }

  @Override
  public SmallMask or(SmallMask other) {
    if (other.isEmpty()) {
      return this;
    }
    return new SmallMask(container.or(other.container));
  }

  @Override
  public SmallMask orNot(SmallMask other, int max) {
    if (other.isEmpty()) {
      return new SmallMask(RunContainer.rangeOfOnes(0, max));
    }
    return new SmallMask(container.or(other.container.not(0, max)));
  }

  @Override
  public SmallMask inPlaceOr(SmallMask other) {
    if (other.isEmpty()) {
      return this;
    }
    var or = container.ior(other.container);
    if (or != container) {
      this.container = or;
    }
    return this;
  }

  @Override
  public SmallMask resetTo(Mask<SmallMask> other) {
    return inPlaceOr(other.unwrap());
  }

  @Override
  public SmallMask unwrap() {
    return this;
  }

  @Override
  public IntStream stream() {
    PeekableShortIterator it = container.getShortIterator();
    return IntStream.range(0, container.getCardinality()).map(i -> it.nextAsInt());
  }

  @Override
  public int first() {
    return container.first();
  }

  @Override
  public SmallMask clone() {
    return new SmallMask(container.clone());
  }

  @Override
  public void optimise() {
    this.container = container.runOptimize();
  }

  @Override
  public boolean isEmpty() {
    return container.isEmpty();
  }

  @Override
  public int cardinality() {
    return container.getCardinality();
  }

  @Override
  public String toString() {
    return container.toString();
  }

  @Override
  public int hashCode() {
    return container.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof SmallMask) {
      return container.equals(((SmallMask) other).container);
    }
    return false;
  }

  private static final class Factory implements MaskFactory<SmallMask> {
    private final SmallMask EMPTY = empty();

    @Override
    public SmallMask empty() {
      return new SmallMask(new ArrayContainer(0));
    }

    @Override
    public SmallMask contiguous(int max) {
      return new SmallMask(RunContainer.rangeOfOnes(0, max));
    }

    @Override
    public SmallMask of(int... values) {
      SmallMask mask = new SmallMask(values.length > 4096 ? new BitmapContainer() : new ArrayContainer());
      for (int v : values) {
        mask.add(v);
      }
      return mask;
    }

    @Override
    public SmallMask emptySingleton() {
      return EMPTY;
    }
  }
}
