package uk.co.openkappa.bitrules.nodes;

import uk.co.openkappa.bitrules.Operation;
import org.roaringbitmap.ArrayContainer;
import org.roaringbitmap.Container;

import java.util.Arrays;

public class DoubleNode {

  private static final Container EMPTY = new ArrayContainer();

  private final Operation relation;

  private double[] thresholds = new double[16];
  private Container[] sets = new Container[16];
  private int count = 0;

  public DoubleNode(Operation relation) {
    this.relation = relation;
  }

  public void add(double threshold, short priority) {
    int position = Arrays.binarySearch(thresholds, 0, count, threshold);
    int insertionPoint = -(position + 1);
    if (position < 0 && insertionPoint < count) {
      incrementCount();
      for (int i = count; i > insertionPoint; --i) {
        sets[i] = sets[i - 1];
        thresholds[i] = thresholds[i - 1];
      }
      sets[insertionPoint] = new ArrayContainer().add(priority);
      thresholds[insertionPoint] = threshold;
    } else if (position < 0) {
      sets[count] = new ArrayContainer().add(priority);
      thresholds[count] = threshold;
      incrementCount();
    } else {
      sets[position] = sets[position].add(priority);
    }
  }

  public DoubleNode optimise() {
    switch (relation) {
      case LT:
      case LE:
        reverseRangeEncode();
        break;
      case GT:
      case GE:
        rangeEncode();
        break;
      default:
    }
    trim();
    return this;
  }

  public Container match(double value, Container context) {
    switch (relation) {
      case GT:
      case GE:
        return context.iand(findRangeEncoded(value));
      case LT:
      case LE:
        return context.iand(findReverseRangeEncoded(value));
      case EQ:
        return context.iand(findEqualityEncoded(value));
      default:
        return context;
    }
  }

  private Container findEqualityEncoded(double value) {
    int index = Arrays.binarySearch(thresholds, 0, count, value);
    return index >= 0 ? sets[index] : EMPTY;
  }

  private Container findRangeEncoded(double value) {
    int pos = Arrays.binarySearch(thresholds, 0, count, value);
    int index = (pos >= 0 ? pos : -(pos + 1)) - 1;
    return index >= 0 && index < count ? sets[index] : EMPTY;
  }

  private Container findReverseRangeEncoded(double value) {
    int pos = Arrays.binarySearch(thresholds, 0, count, value);
    int index = (pos >= 0 ? pos + 1 : -(pos + 1));
    return index >= 0 && index < count ? sets[index] : EMPTY;
  }

  private void reverseRangeEncode() {
    for (int i = count - 2; i >= 0; --i) {
      sets[i] = sets[i].ior(sets[i + 1]);
    }
  }

  private void rangeEncode() {
    for (int i = 1; i < count; ++i) {
      sets[i] = sets[i].ior(sets[i - 1]);
    }
  }

  private void trim() {
    thresholds = Arrays.copyOf(thresholds, count);
    sets = Arrays.copyOf(sets, count);
  }

  private void incrementCount() {
    ++count;
    if (count == thresholds.length) {
      sets = Arrays.copyOf(sets, count * 2);
      thresholds = Arrays.copyOf(thresholds, count * 2);
    }
  }
}
