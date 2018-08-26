package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Operation;

import java.util.EnumMap;
import java.util.Map;

public class CompositeLongNode<MaskType extends Mask<MaskType>> {

  private final Map<Operation, LongNode<MaskType>> children = new EnumMap<>(Operation.class);
  private final MaskType empty;

  public CompositeLongNode(MaskType empty) {
    this.empty = empty;
  }

  public void add(Operation relation, long threshold, int priority) {
    children.computeIfAbsent(relation, r -> new LongNode<>(r, empty)).add(threshold, priority);
  }

  public MaskType match(long value, MaskType result) {
    MaskType temp = empty.clone();
    for (LongNode<MaskType> component : children.values()) {
      temp = temp.inPlaceOr(component.apply(value, result.clone()));
    }
    return result.inPlaceAnd(temp);
  }

  public void optimise() {
    children.values().forEach(LongNode::optimise);
  }

}
