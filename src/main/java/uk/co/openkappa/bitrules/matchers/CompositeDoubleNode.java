package uk.co.openkappa.bitrules.matchers;


import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Masks;
import uk.co.openkappa.bitrules.Operation;

import java.util.EnumMap;
import java.util.Map;

public class CompositeDoubleNode<MaskType extends Mask<MaskType>> {

  private final Map<Operation, DoubleNode<MaskType>> children = new EnumMap<>(Operation.class);
  private final MaskType empty;

  public CompositeDoubleNode(Class<MaskType> type) {
    this.empty = Masks.create(type);
  }

  public void add(Operation relation, double threshold, int priority) {
    children.computeIfAbsent(relation, r -> new DoubleNode<>(r, empty))
              .add(threshold, priority);
  }

  public MaskType match(double value, MaskType result) {
    MaskType temp = empty.clone();
    for (DoubleNode<MaskType> component : children.values()) {
      temp = temp.inPlaceOr(component.match(value, result.clone()));
    }
    return result.inPlaceAnd(temp);
  }

  public void optimise() {
    children.values().forEach(DoubleNode::optimise);
  }

}
