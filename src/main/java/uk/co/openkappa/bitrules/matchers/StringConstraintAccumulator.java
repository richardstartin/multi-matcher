package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.Constraint;
import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.masks.MaskFactory;
import uk.co.openkappa.bitrules.matchers.nodes.ComparableNode;
import uk.co.openkappa.bitrules.matchers.nodes.PrefixNode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static uk.co.openkappa.bitrules.Operation.STARTS_WITH;

public class StringConstraintAccumulator<Input, MaskType extends Mask<MaskType>>
        extends GenericConstraintAccumulator<Input, String, MaskType> {



  public StringConstraintAccumulator(Function<Input, String> accessor, MaskFactory<MaskType> maskFactory, int max) {
    this(HashMap::new, accessor, maskFactory, max);
  }

  private StringConstraintAccumulator(Supplier<Map<String, MaskType>> mapSupplier, Function<Input, String> accessor, MaskFactory<MaskType> maskFactory, int max) {
    super(mapSupplier, accessor, maskFactory, max);
  }

  @Override
  public boolean addConstraint(Constraint constraint, int priority) {
    if (super.addConstraint(constraint, priority)) {
      return true;
    }
    switch (constraint.getOperation()) {
      case STARTS_WITH:
        var prefix = (PrefixNode<MaskType>) nodes.computeIfAbsent(STARTS_WITH,
                o -> new PrefixNode<>(empty));
        prefix.add(constraint.getValue(), priority);
        break;
      case GT:
      case GE:
      case LT:
      case LE:
        var comparable = (ComparableNode<String, MaskType>) nodes.computeIfAbsent(STARTS_WITH,
                o -> new ComparableNode<String, MaskType>(nullsFirst(naturalOrder()), constraint.getOperation(), empty));
        comparable.add(constraint.getValue(), priority);
        break;
      default:
        return false;
    }
    wildcard.remove(priority);
    return true;
  }

}
