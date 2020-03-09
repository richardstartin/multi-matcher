package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.Constraint;
import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.Matcher;
import io.github.richardstartin.multimatcher.core.Operation;
import io.github.richardstartin.multimatcher.core.masks.MaskFactory;
import io.github.richardstartin.multimatcher.core.matchers.nodes.PrefixNode;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.richardstartin.multimatcher.core.Operation.*;

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
    return new StringMatcher<>(accessor, frozen, wildcard, empty);
  }

  private static class StringMatcher<T, MaskType extends Mask<MaskType>> implements Matcher<T, MaskType> {

    private final Function<T, String> accessor;
    private final EnumMap<Operation, ClassificationNode<String, MaskType>> nodes;
    private final MaskType wildcard;
    private final MaskType empty;

    StringMatcher(Function<T, String> accessor,
                  EnumMap<Operation, ClassificationNode<String, MaskType>> nodes,
                  MaskType wildcard,
                  MaskType empty) {
      this.accessor = accessor;
      this.nodes = nodes;
      this.wildcard = wildcard;
      this.empty = empty;
    }

    @Override
    public void match(T input, MaskType context) {
      String value = accessor.apply(input);
      var temp = empty.clone();
      match(EQ, temp, value);
      match(STARTS_WITH, temp, value);
      matchNotEquals(context, value);
      context.inPlaceAnd(temp.inPlaceOr(wildcard));
    }

    private void matchNotEquals(MaskType context, String value) {
      var node = nodes.get(NE);
      if (null != node) {
        context.inPlaceAnd(node.match(value));
      }
    }

    private void match(Operation op, MaskType context, String value) {
      var node = nodes.get(op);
      if (null != node) {
        context.inPlaceOr(node.match(value));
      }
    }
  }

}
