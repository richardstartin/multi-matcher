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
import static io.github.richardstartin.multimatcher.core.matchers.Utils.newArray;

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
    if (constraint.getOperation() == STARTS_WITH) {
      var prefix = (PrefixNode<MaskType>) nodes.computeIfAbsent(STARTS_WITH,
              o -> new PrefixNode<>(empty));
      prefix.add(constraint.getValue(), priority);
    } else {
      return false;
    }
    wildcard.remove(priority);
    return true;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Matcher<Input, MaskType> freeze() {
    wildcard.optimise();
    var frozen = (ClassificationNode<String, MaskType>[])newArray(ClassificationNode.class, SIZE);
    for (var node : nodes.values()) {
      node.link(nodes);
    }
    for (var pair : nodes.entrySet()) {
      frozen[pair.getKey().ordinal()] = pair.getValue().freeze();
    }
    return new StringMatcher<>(accessor, frozen, wildcard, empty);
  }

  private static class StringMatcher<T, MaskType extends Mask<MaskType>> implements Matcher<T, MaskType> {

    private final Function<T, String> accessor;
    private final ClassificationNode<String, MaskType>[] nodes;
    private final MaskType wildcard;
    private final ThreadLocal<MaskType> empty;

    StringMatcher(Function<T, String> accessor,
                  ClassificationNode<String, MaskType>[] nodes,
                  MaskType wildcard,
                  MaskType empty) {
      this.accessor = accessor;
      this.nodes = nodes;
      this.wildcard = wildcard;
      this.empty = ThreadLocal.withInitial(empty::clone);
    }

    @Override
    public void match(T input, MaskType context) {
      String value = accessor.apply(input);
      var temp = empty.get();
      match(EQ, temp, value);
      match(STARTS_WITH, temp, value);
      matchNotEquals(context, value);
      context.inPlaceAnd(temp.inPlaceOr(wildcard));
      temp.clear();
    }

    private void matchNotEquals(MaskType context, String value) {
      var node = nodes[NE.ordinal()];
      if (null != node) {
        context.inPlaceAnd(node.match(value));
      }
    }

    private void match(Operation op, MaskType context, String value) {
      var node = nodes[op.ordinal()];
      if (null != node) {
        context.inPlaceOr(node.match(value));
      }
    }
  }
}
