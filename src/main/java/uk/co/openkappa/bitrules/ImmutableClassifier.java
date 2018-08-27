package uk.co.openkappa.bitrules;


import uk.co.openkappa.bitrules.config.Schema;

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
   * Gets a new builder for a classifier
   *
   * @param <Key>            the create key type
   * @param <Input>          the type of the classified objects
   * @param <Classification> the classification type
   * @return a new classifier builder
   */
  public static <Key, Input, Classification>
  ClassifierBuilder<Key, Input, Classification> builder(Schema<Key, Input> schema) {
    return new ClassifierBuilder<>(schema);
  }

  @Override
  public Stream<Classification> classifications(Input input) {
    return impl.classifications(input);
  }

  @Override
  public Optional<Classification> classification(Input input) {
    return impl.classification(input);
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
     * @param constraints the matching constraints
     * @return the classifier
     */
    public ImmutableClassifier<Input, Classification> build(List<MatchingConstraint<Key, Classification>> constraints) {
      int maxPriority = constraints.size();
      return maxPriority < WordMask.MAX_CAPACITY
              ? new ImmutableClassifier<>(build(constraints, WordMask.contiguous(maxPriority), WordMask.class, maxPriority))
              : maxPriority < ContainerMask.MAX_CAPACITY
                ? new ImmutableClassifier<>(build(constraints, ContainerMask.contiguous(maxPriority), ContainerMask.class, maxPriority))
                : new ImmutableClassifier<>(build(constraints, RoaringBitmapMask.contiguous(maxPriority), RoaringBitmapMask.class, maxPriority));
    }

    private <MaskType extends Mask<MaskType>>
    MaskedClassifier<MaskType, Input, Classification> build(List<MatchingConstraint<Key, Classification>> specs,
                                                            MaskType mask,
                                                            Class<MaskType> type,
                                                            int max) {
      PrimitiveIterator.OfInt sequence = IntStream.iterate(0, i -> i + 1).iterator();
      specs.stream().sorted(Comparator.comparingInt(rd -> order(rd.getPriority())))
                    .forEach(rule -> addMatchingConstraint(rule, sequence.nextInt(), type, max));
      return new MaskedClassifier<>(classifications, freezeMatchers(), mask);
    }

    private <MaskType extends Mask<MaskType>>
    void addMatchingConstraint(MatchingConstraint<Key, Classification> matchInfo,
                               int priority,
                               Class<MaskType> type,
                               int max) {
      classifications.add(matchInfo.getClassification());
      matchInfo.getConstraints().forEach((key, condition) -> memoisedMatcher(key, type, max).addConstraint(condition, priority));
    }

    private <MaskType extends Mask<MaskType>>
    Matcher<Input, MaskType> memoisedMatcher(Key key, Class<MaskType> type, int max) {
      if (!matchers.containsKey(key)) {
        matchers.put(key, registry.getAttribute(key).toMatcher(type, max));
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
