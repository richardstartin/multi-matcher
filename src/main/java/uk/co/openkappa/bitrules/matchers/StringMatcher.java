package uk.co.openkappa.bitrules.matchers;

import org.apache.commons.collections4.trie.PatriciaTrie;
import uk.co.openkappa.bitrules.*;
import uk.co.openkappa.bitrules.masks.MaskFactory;
import uk.co.openkappa.bitrules.structures.PerfectHashMap;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static uk.co.openkappa.bitrules.Mask.with;
import static uk.co.openkappa.bitrules.Operation.EQ;
import static uk.co.openkappa.bitrules.Operation.STARTS_WITH;

public class StringMatcher<Input, MaskType extends Mask<MaskType>> implements MutableMatcher<Input, MaskType> {

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
        GenericEqualityNode<String, MaskType> literal = (GenericEqualityNode<String, MaskType>) nodes.computeIfAbsent(EQ,
                o -> new GenericEqualityNode<>(mapSupplier.get(), empty, wildcards, PerfectHashMap::wrap));
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

  @Override
  public float averageSelectivity() {
    float avg = 0;
    int count = 0;
    for (var node : nodes.values()) {
      avg += node.averageSelectivity();
      ++count;
    }
    return avg / count;
  }

  private static class PrefixNode<MaskType extends Mask<MaskType>> implements MutableNode<String, MaskType> {

    private final MaskType empty;
    private final Map<String, MaskType> map;
    private int longest;

    private PrefixNode(MaskType empty) {
      this(empty, new HashMap<>(), 0);
    }

    private PrefixNode(MaskType empty, Map<String, MaskType> map, int longest) {
      this.empty = empty;
      this.map = map;
      this.longest = longest;
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
    public ClassificationNode<String, MaskType> optimise() {
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
      return new PrefixNode<>(empty, PerfectHashMap.wrap(map), longest);
    }

    public void add(String prefix, int id) {
      map.compute(prefix, (p, mask) -> with(null == mask ? empty.clone() : mask, id));
    }
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
  }

}
