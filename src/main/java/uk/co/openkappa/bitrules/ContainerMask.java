package uk.co.openkappa.bitrules;

import org.roaringbitmap.ArrayContainer;
import org.roaringbitmap.Container;
import org.roaringbitmap.PeekableShortIterator;
import org.roaringbitmap.RunContainer;

import java.util.stream.IntStream;


public class ContainerMask implements Mask<ContainerMask> {

  public static ContainerMask contiguous(int to) {
    return new ContainerMask(RunContainer.rangeOfOnes(0, to));
  }

  public static ContainerMask full() {
    return new ContainerMask(RunContainer.full());
  }

  private Container container;

  public ContainerMask(Container container) {
    this.container = container;
  }

  public ContainerMask() {
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
  public ContainerMask and(ContainerMask other) {
    return new ContainerMask(container.and(other.container));
  }

  @Override
  public ContainerMask andNot(ContainerMask other) {
    return new ContainerMask(container.andNot(other.container));
  }

  @Override
  public ContainerMask inPlaceAnd(ContainerMask other) {
    this.container = container.iand(other.container);
    return this;
  }

  @Override
  public ContainerMask or(ContainerMask other) {
    return new ContainerMask(container.or(other.container));
  }

  @Override
  public ContainerMask inPlaceOr(ContainerMask other) {
    this.container = container.ior(other.container);
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
  public ContainerMask clone() {
    return new ContainerMask(container.clone());
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
  public String toString() {
    return container.toString();
  }

  @Override
  public int hashCode() {
    return container.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof ContainerMask) {
      return container.equals(((ContainerMask) other).container);
    }
    return false;
  }
}
