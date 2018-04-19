package com.openkappa.bitrules.nodes;

import com.openkappa.bitrules.LongRelation;
import org.roaringbitmap.ArrayContainer;
import org.roaringbitmap.Container;

import java.util.Arrays;

public class LongNode {

  private static final Container EMPTY = new ArrayContainer();

  private final LongRelation relation;

  private long[] thresholds = new long[16];
  private Container[] containers = new Container[16];
  private int count = 0;

  public LongNode(LongRelation relation) {
    this.relation = relation;
  }

  public void add(long value, short priority) {
    int position = Arrays.binarySearch(thresholds, 0, count, value);
    int insertionPoint = -(position + 1);
    if (position < 0 && insertionPoint < count) {
      incrementCount();
      for (int i = count; i > insertionPoint; --i) {
        containers[i] = containers[i - 1];
        thresholds[i] = thresholds[i - 1];
      }
      containers[insertionPoint] = new ArrayContainer().add(priority);
      thresholds[insertionPoint] = value;
    } else if (position < 0) {
      containers[count] = new ArrayContainer().add(priority);
      thresholds[count] = value;
      incrementCount();
    } else {
      containers[position] = containers[position].add(priority);
    }
  }

  public Container apply(long value, Container context) {
    switch (relation) {
      case GT:
        return context.iand(checkForwards(value));
      case LT:
        return context.iand(checkBackwards(value));
      case EQ:
        return context.iand(checkOne(value));
      default:
        return context.iand(checkAll(value));
    }
  }

  private Container checkOne(long value) {
    int index = Arrays.binarySearch(thresholds, 0, count, value);
    return index >= 0 ? containers[index] : EMPTY;
  }

  private Container checkAll(long value) {
    Container temp = new ArrayContainer();
    int c = 0;
    while (c < count) {
      if (relation.test(value, thresholds[c])) {
        temp = temp.ior(containers[c++]);
      }
    }
    return temp;
  }

  private Container checkForwards(long value) {
    Container temp = new ArrayContainer();
    int i = 0;
    while (i < count && relation.test(value, thresholds[i])) {
      temp = temp.ior(containers[i++]);
    }
    return temp;
  }

  private Container checkBackwards(long value) {
    Container temp = new ArrayContainer();
    int i = count - 1;
    while (i >= 0 && relation.test(value, thresholds[i])) {
      temp = temp.ior(containers[i--]);
    }
    return temp;
  }

  private void incrementCount() {
    ++count;
    if (count == thresholds.length) {
      containers = Arrays.copyOf(containers, count * 2);
      thresholds = Arrays.copyOf(thresholds, count * 2);
    }
  }
}
