package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.*;
import uk.co.openkappa.bitrules.masks.MaskFactory;
import uk.co.openkappa.bitrules.matchers.nodes.GenericEqualityNode;
import uk.co.openkappa.bitrules.matchers.nodes.PrefixNode;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static uk.co.openkappa.bitrules.Operation.EQ;
import static uk.co.openkappa.bitrules.Operation.STARTS_WITH;
import static uk.co.openkappa.bitrules.matchers.SelectivityHeuristics.avgCardinality;

public class StringMatcher<Input, MaskType extends Mask<MaskType>>
        implements ConstraintAccumulator<Input, MaskType>, Matcher<Input, MaskType> {

  private final EnumMap<Operation, MutableNode<String, MaskType>> nodes = new EnumMap<>(Operation.class);
  private final Supplier<Map<String, MaskType>> mapSupplier;
  private final Function<Input, String> accessor;
  private final MaskType wildcards;
  private final MaskType empty;

  public StringMatcher(Function<Input, String> accessor, MaskFactory<MaskType> maskFactory, int max) {
    this(HashMap::new, accessor, maskFactory, max);
  }

  private StringMatcher(Supplier<Map<String, MaskType>> mapSupplier, Function<Input, String> accessor, MaskFactory<MaskType> maskFactory, int max) {
    this.accessor = accessor;
    this.empty = maskFactory.emptySingleton();
    this.wildcards = maskFactory.contiguous(max);
    this.mapSupplier = mapSupplier;
  }

  @Override
  public MaskType match(Input input, MaskType context) {
    String value = accessor.apply(input);
    MaskType result = empty.clone();
    for (MutableNode<String, MaskType> component : nodes.values()) {
      result = result.inPlaceOr(component.match(value, context.clone()));
    }
    return result.inPlaceAnd(context.or(wildcards));
  }

  @Override
  public void addConstraint(Constraint constraint, int priority) {
    switch (constraint.getOperation()) {
      case STARTS_WITH:
        PrefixNode<MaskType> prefix = (PrefixNode<MaskType>) nodes.computeIfAbsent(STARTS_WITH,
                o -> new PrefixNode<>(empty));
        prefix.add(constraint.getValue(), priority);
        break;
      case EQ:
        var literal = (GenericEqualityNode<String, MaskType>) nodes.computeIfAbsent(EQ,
                o -> new GenericEqualityNode<>(mapSupplier.get(), empty, wildcards));
        literal.add(constraint.getValue(), priority);
        break;
      default:
        throw new IllegalStateException("Unsupported for String matching: " + constraint.getOperation());
    }
    wildcards.remove(priority);
  }

  @Override
  public Matcher<Input, MaskType> freeze() {
    return new OptimisedStringMatcher<>(this);
  }

  private static class OptimisedStringMatcher<Input, MaskType extends Mask<MaskType>> implements Matcher<Input, MaskType> {

    private final EnumMap<Operation, ClassificationNode<String, MaskType>> nodes = new EnumMap<>(Operation.class);
    private final Function<Input, String> accessor;
    private final MaskType wildcards;
    private final MaskType empty;

    private OptimisedStringMatcher(StringMatcher<Input, MaskType> unoptimised) {
      this.accessor = unoptimised.accessor;
      this.wildcards = unoptimised.wildcards;
      this.empty = unoptimised.empty;
      for (Map.Entry<Operation, MutableNode<String, MaskType>> entry : unoptimised.nodes.entrySet()) {
        this.nodes.put(entry.getKey(), entry.getValue().optimise());
      }
      wildcards.optimise();
    }

    @Override
    public MaskType match(Input input, MaskType context) {
      String value = accessor.apply(input);
      MaskType result = empty.clone();
      for (ClassificationNode<String, MaskType> component : nodes.values()) {
        result = result.inPlaceOr(component.match(value, context.clone()));
      }
      return result.inPlaceAnd(context.or(wildcards));
    }

    @Override
    public float averageSelectivity() {
      return avgCardinality(nodes.values(), ClassificationNode::averageSelectivity);
    }
  }

}
