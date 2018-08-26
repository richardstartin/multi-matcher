package uk.co.openkappa.bitrules;


import uk.co.openkappa.bitrules.config.Schema;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Immutable classifier. A new instance must be built if matchers are updated.
 *
 * @param <Input> the type of the classified objects
 * @param <Classification> the type of the resultant classification
 */
public class ImmutableClassifier<Input, Classification> implements Classifier<Input, Classification> {

  private final Classifier<Input, Classification> impl;

  ImmutableClassifier(Classifier<Input, Classification> impl) {
    this.impl = impl;
  }

  /**
   * Gets a new newRule for a classifier
   *
   * @param <Key>            the schema key type
   * @param <Input>          the type of the classified objects
   * @param <Classification> the classification type
   * @return a new newRule
   */
  public static <Key, Input, Classification>
  ClassifierBuilder<Key, Input, Classification> definedBy(Schema<Key, Input> schema) {
    return new ClassifierBuilder<>(schema);
  }

  @Override
  public Stream<Classification> classify(Input input) {
    return impl.classify(input);
  }

  @Override
  public Optional<Classification> getBestClassification(Input input) {
    return impl.getBestClassification(input);
  }

  public static class ClassifierBuilder<Key, Input, Classification> {

    private final Schema<Key, Input> registry;
    private final Map<Key, Matcher<Input, ? extends Mask>> matchers = new HashMap<>();
    private final List<Classification> classifications = new ArrayList<>();

    public ClassifierBuilder(Schema<Key, Input> registry) {
      this.registry = registry;
    }

    /**
     * Build a classifier from some matchers
     *
     * @param repository the container of matchers
     * @return the classifier built from the current snapshot of the repository and attribute registry
     * @throws IOException if the repository throws
     */
    public ImmutableClassifier<Input, Classification> build(RuleSpecifications<Key, Classification> repository) throws IOException {
      List<MatchingConstraint<Key, Classification>> specs = repository.specifications();
      int maxPriority = specs.size();
      return maxPriority < TinyMask.MAX_CAPACITY
              ? new ImmutableClassifier<>(build(specs, TinyMask.contiguous(maxPriority), TinyMask.class))
              : new ImmutableClassifier<>(build(specs, ContainerMask.contiguous(maxPriority), ContainerMask.class));
    }

    private <MaskType extends Mask<MaskType>>
    MaskedClassifier<MaskType, Input, Classification> build(List<MatchingConstraint<Key, Classification>> specs, MaskType mask, Class<MaskType> type) {
      PrimitiveIterator.OfInt sequence = IntStream.iterate(0, i -> i + 1).iterator();
      specs.stream().sorted(Comparator.comparingInt(rd -> order(rd.getPriority())))
                    .forEach(rule -> addMatchingConstraint(rule, sequence.nextInt(), type));
      return new MaskedClassifier<>(classifications, freezeMatchers(), mask);
    }

    private <MaskType extends Mask<MaskType>>
    void addMatchingConstraint(MatchingConstraint<Key, Classification> matchInfo, int priority, Class<MaskType> type) {
      classifications.add(matchInfo.getClassification());
      matchInfo.getConstraints()
              .forEach((key, condition) -> memoisedMatcher(key, type).addConstraint(condition, priority));
    }

    private <MaskType extends Mask<MaskType>>
    Matcher<Input, MaskType> memoisedMatcher(Key key, Class<MaskType> type) {
      if (!matchers.containsKey(key)) {
        matchers.put(key, registry.getAttribute(key).toMatcher(type));
      }
      return (Matcher<Input, MaskType>) matchers.get(key);
    }

    private <MaskType extends Mask<MaskType>>
    List<Matcher<Input, MaskType>> freezeMatchers() {
      List<Matcher<Input, MaskType>> frozen = new ArrayList<>(matchers.size());
      for (Matcher<Input, ? extends Mask> matcher : matchers.values()) {
        matcher.freeze();
        frozen.add((Matcher<Input, MaskType>) matcher);
      }
      return frozen;
    }

    private static int order(int priority) {
      return (1 << 16) - priority - 1;
    }
  }

}
