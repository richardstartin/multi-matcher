package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Operation;

import java.util.EnumMap;
import java.util.Map;

public class CompositeIntNode<MaskType extends Mask<MaskType>> {

  private final Map<Operation, IntNode<MaskType>> children = new EnumMap<>(Operation.class);
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
