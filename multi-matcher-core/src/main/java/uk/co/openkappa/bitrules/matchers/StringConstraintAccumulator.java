package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.Constraint;
import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Matcher;
import uk.co.openkappa.bitrules.Operation;
import uk.co.openkappa.bitrules.masks.MaskFactory;
import uk.co.openkappa.bitrules.matchers.nodes.PrefixNode;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static uk.co.openkappa.bitrules.Operation.*;

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
      default:
        return false;
    }
    wildcard.remove(priority);
    return true;
  }

  @Override
  public Matcher<Input, MaskType> freeze() {
    wildcard.optimise();
    EnumMap<Operation, ClassificationNode<String, MaskType>> frozen = new EnumMap<>(Operation.class);
    nodes.forEach((op, node) -> node.link(nodes));
    nodes.forEach((op, node) -> frozen.put(op, node.freeze()));
    return new StringMatcher<>(accessor, frozen, wildcard);
  }

  private static class StringMatcher<T, MaskType extends Mask<MaskType>> implements Matcher<T, MaskType> {

    private final Function<T, String> accessor;
    private final EnumMap<Operation, ClassificationNode<String, MaskType>> nodes;
    private final MaskType wildcard;

    StringMatcher(Function<T, String> accessor, EnumMap<Operation, ClassificationNode<String, MaskType>> nodes, MaskType wildcard) {
      this.accessor = accessor;
      this.nodes = nodes;
      this.wildcard = wildcard;
    }

    @Override
    public MaskType match(T input, MaskType context) {
      String value = accessor.apply(input);
      MaskType result = context.clone();
      var eq = nodes.get(EQ);
      var prefix = nodes.get(STARTS_WITH);
      if (null != prefix) {
        result = result.inPlaceAnd(prefix.match(value));
        if (null != eq) {
          result = result.inPlaceOr(eq.match(value));
        }
        result = result.inPlaceOr(wildcard);
      } else if (null != eq) {
        result = result.inPlaceAnd(eq.match(value)).inPlaceOr(wildcard);
      }
      var neq = nodes.get(NE);
      if (null != neq) {
        result = result.inPlaceAnd(neq.match(value));
      }
      return context.inPlaceAnd(result);
    }
  }

}
