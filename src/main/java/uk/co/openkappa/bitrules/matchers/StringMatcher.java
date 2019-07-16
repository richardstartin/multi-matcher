package uk.co.openkappa.bitrules.matchers;

import org.apache.commons.collections4.trie.PatriciaTrie;
import uk.co.openkappa.bitrules.Constraint;
import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Matcher;
import uk.co.openkappa.bitrules.Operation;
import uk.co.openkappa.bitrules.masks.MaskFactory;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static uk.co.openkappa.bitrules.Mask.with;
import static uk.co.openkappa.bitrules.Operation.EQ;
import static uk.co.openkappa.bitrules.Operation.STARTS_WITH;

public class StringMatcher<Input, MaskType extends Mask<MaskType>> implements Matcher<Input, MaskType> {

  private final EnumMap<Operation, Node<String, MaskType>> nodes = new EnumMap<>(Operation.class);
  private final Supplier<Map<String, MaskType>> mapSupplier;
  private final Function<Input, String> accessor;
  private final MaskType wildcards;
  private final MaskType empty;

  public StringMatcher(Function<Input, String> accessor, MaskFactory<MaskType> maskFactory, int max) {
    this(HashMap::new, accessor, maskFactory, max);
  }

  public StringMatcher(Supplier<Map<String, MaskType>> mapSupplier, Function<Input, String> accessor, MaskFactory<MaskType> maskFactory, int max) {
    this.accessor = accessor;
    this.empty = maskFactory.emptySingleton();
    this.wildcards = maskFactory.contiguous(max);
    this.mapSupplier = mapSupplier;
  }

  @Override
  public MaskType match(Input input, MaskType context) {
    String value = accessor.apply(input);
    MaskType result = empty.clone();
    for (Node<String, MaskType> component : nodes.values()) {
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
        GenericEqualityNode<String, MaskType> literal = (GenericEqualityNode<String, MaskType>) nodes.computeIfAbsent(EQ,
                o -> new GenericEqualityNode<>(mapSupplier.get(), empty, wildcards));
        literal.add(constraint.getValue(), priority);
        break;
      default:
        throw new IllegalStateException("Unsupported for String matching: " + constraint.getOperation());
    }
    wildcards.remove(priority);
  }

  @Override
  public void freeze() {
    wildcards.optimise();
    nodes.values().forEach(Node::optimise);
  }

  private static class PrefixNode<MaskType extends Mask<MaskType>> implements Node<String, MaskType> {

    private final MaskType empty;
    private final Map<String, MaskType> map;
    private int longest;

    private PrefixNode(MaskType empty) {
      this.empty = empty;
      this.map = new HashMap<>();
    }

    @Override
    public MaskType match(String value, MaskType context) {
      int position = longest;
      while (position > 0 && !context.isEmpty()) {
        MaskType match = map.get(value.substring(0, position));
        if (null != match) {
          return context.inPlaceAnd(match);
        }
        --position;
      }
      return context.inPlaceAnd(empty);
    }

    @Override
    public void optimise() {
      this.longest = map.keySet().stream().mapToInt(String::length).max().orElse(0);
      PatriciaTrie<MaskType> trie = new PatriciaTrie<>();
      trie.putAll(map);
      map.keySet().stream().sorted(Comparator.comparingInt(String::length))
         .forEach(key -> {
           MaskType mask = map.get(key);
           trie.prefixMap(key)
               .forEach((k, v) -> {
                 if (!key.equals(k)) {
                   v.inPlaceOr(mask);
                 }
               });
         });
    }

    public void add(String prefix, int id) {
      map.compute(prefix, (p, mask) -> with(null == mask ? empty.clone() : mask, id));
    }
  }

}
