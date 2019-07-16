package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.Constraint;
import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Matcher;
import uk.co.openkappa.bitrules.Operation;
import uk.co.openkappa.bitrules.masks.MaskFactory;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.function.ToIntFunction;

public class IntMatcher<T, MaskType extends Mask<MaskType>> implements Matcher<T, MaskType> {

  private final ToIntFunction<T> accessor;
  private final CompositeIntNode<MaskType> node;
  private final MaskType wildcards;

  public IntMatcher(ToIntFunction<T> accessor, MaskFactory<MaskType> maskFactory, int max) {
    this.accessor = accessor;
    this.node = new CompositeIntNode<>(maskFactory.emptySingleton());
    this.wildcards = maskFactory.contiguous(max);
  }

  @Override
  public MaskType match(T value, MaskType context) {
    MaskType result = node.match(accessor.applyAsInt(value), context);
    return context.inPlaceAnd(result.inPlaceOr(wildcards));
  }

  @Override
  public void addConstraint(Constraint constraint, int priority) {
    Number number = constraint.getValue();
    int value = number.intValue();
    node.add(constraint.getOperation(), value, priority);
    wildcards.remove(priority);
  }

  @Override
  public void freeze() {
    node.optimise();
    wildcards.optimise();
  }

  public static class CompositeIntNode<MaskType extends Mask<MaskType>> {

    private final EnumMap<Operation, IntNode<MaskType>> children = new EnumMap<>(Operation.class);
    private final MaskType empty;

    public CompositeIntNode(MaskType empty) {
      this.empty = empty;
    }


    public void add(Operation relation, int threshold, int priority) {
      children.computeIfAbsent(relation, r -> new IntNode<>(r, empty)).add(threshold, priority);
    }

    public MaskType match(int value, MaskType result) {
      MaskType temp = empty.clone();
      for (IntNode<MaskType> component : children.values()) {
        temp = temp.inPlaceOr(component.apply(value, result.clone()));
      }
      return result.inPlaceAnd(temp);
    }

    public void optimise() {
      children.values().forEach(IntNode::optimise);
    }
  }

  public static class IntNode<MaskType extends Mask<MaskType>> {

    private final Operation relation;
    private final MaskType empty;

    private int[] thresholds = new int[16];
    private MaskType[] sets;
    private int count = 0;

    public IntNode(Operation relation, MaskType empty) {
      this.relation = relation;
      this.empty = empty;
      this.sets = (MaskType[]) Array.newInstance(empty.getClass(), 16);
    }

    public void add(int value, int priority) {
      if (count > 0 && value > thresholds[count - 1]) {
        ensureCapacity();
        int position = count;
        MaskType mask = sets[position];
        if (null == mask) {
          mask = empty.clone();
        }
        mask.add(priority);
        thresholds[position] = value;
        sets[position] = mask;
        ++count;
      } else {
        int position = Arrays.binarySearch(thresholds, 0, count, value);
        int insertionPoint = -(position + 1);
        if (position < 0 && insertionPoint < count) {
          ensureCapacity();
          for (int i = count; i > insertionPoint; --i) {
            sets[i] = sets[i - 1];
            thresholds[i] = thresholds[i - 1];
          }
          sets[insertionPoint] = maskWith(priority);
          thresholds[insertionPoint] = value;
          ++count;
        } else if (position < 0) {
          ensureCapacity();
          sets[count] = maskWith(priority);
          thresholds[count] = value;
          ++count;
        } else {
          sets[position].add(priority);
        }
      }
    }

    public MaskType apply(int value, MaskType context) {
      switch (relation) {
        case GT:
          return context.inPlaceAnd(findRangeEncoded(value));
        case GE:
          return context.inPlaceAnd(findRangeEncodedInclusive(value));
        case LT:
          return context.inPlaceAnd(findReverseRangeEncoded(value));
        case LE:
          return context.inPlaceAnd(findReverseRangeEncodedInclusive(value));
        case EQ:
          return context.inPlaceAnd(findEqualityEncoded(value));
        default:
          return context;
      }
    }

    public IntNode<MaskType> optimise() {
      switch (relation) {
        case GE:
        case GT:
          rangeEncode();
          break;
        case LE:
        case LT:
          reverseRangeEncode();
          break;
        default:
      }
      trim();
      return this;
    }

    private MaskType findEqualityEncoded(int value) {
      int index = Arrays.binarySearch(thresholds, 0, count, value);
      return index >= 0 ? sets[index] : empty;
    }

    private MaskType findRangeEncoded(int value) {
      int pos = Arrays.binarySearch(thresholds, 0, count, value);
      int index = (pos >= 0 ? pos : -(pos + 1)) - 1;
      return index >= 0 && index < count ? sets[index] : empty;
    }

    private MaskType findRangeEncodedInclusive(int value) {
      int pos = Arrays.binarySearch(thresholds, 0, count, value);
      int index = (pos >= 0 ? pos : -(pos + 1) - 1);
      return index >= 0 && index < count ? sets[index] : empty;
    }

    private MaskType findReverseRangeEncoded(int value) {
      int pos = Arrays.binarySearch(thresholds, 0, count, value);
      int index = (pos >= 0 ? pos + 1 : -(pos + 1));
      return index >= 0 && index < count ? sets[index] : empty;
    }

    private MaskType findReverseRangeEncodedInclusive(int value) {
      int pos = Arrays.binarySearch(thresholds, 0, count, value);
      int index = (pos >= 0 ? pos : -(pos + 1));
      return index >= 0 && index < count ? sets[index] : empty;
    }

    private void reverseRangeEncode() {
      for (int i = count - 2; i >= 0; --i) {
        sets[i].inPlaceOr(sets[i + 1]).optimise();
      }
    }

    private void rangeEncode() {
      for (int i = 1; i < count; ++i) {
        sets[i].inPlaceOr(sets[i - 1]).optimise();
      }
    }

    private void trim() {
      sets = Arrays.copyOf(sets, count);
      thresholds = Arrays.copyOf(thresholds, count);
    }

    private void ensureCapacity() {
      int newCount = count + 1;
      if (newCount == thresholds.length) {
        sets = Arrays.copyOf(sets, newCount * 2);
        thresholds = Arrays.copyOf(thresholds, newCount * 2);
      }
    }

    private MaskType maskWith(int value) {
      MaskType mask = empty.clone();
      mask.add(value);
      return mask;
    }

    @Override
    public String toString() {
      return Nodes.toString(count, relation,
              Arrays.stream(thresholds).boxed().iterator(),
              Arrays.stream(sets).iterator());
    }
  }
}
