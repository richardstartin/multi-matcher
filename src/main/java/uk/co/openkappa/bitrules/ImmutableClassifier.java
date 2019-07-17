package uk.co.openkappa.bitrules;


import uk.co.openkappa.bitrules.masks.MaskFactory;
import uk.co.openkappa.bitrules.schema.Schema;
import uk.co.openkappa.bitrules.masks.HugeMask;
import uk.co.openkappa.bitrules.masks.SmallMask;
import uk.co.openkappa.bitrules.masks.TinyMask;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Immutable classifier. A new instance must be built if matchers are updated.
 *
 * @param <Input> the type named the classified objects
 * @param <Classification> the type named the resultant classification
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
   * @param <Input>          the type named the classified objects
   * @param <Classification> the classification type
   * @param schema the schema
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
    private final Map<Key, MutableMatcher<Input, ? extends Mask>> matchers = new HashMap<>();
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
      return maxPriority < TinyMask.MAX_CAPACITY
              ? new ImmutableClassifier<>(build(constraints, TinyMask.FACTORY.contiguous(maxPriority), TinyMask.FACTORY, maxPriority))
              : maxPriority < SmallMask.MAX_CAPACITY
                ? new ImmutableClassifier<>(build(constraints, SmallMask.FACTORY.contiguous(maxPriority), SmallMask.FACTORY, maxPriority))
                : new ImmutableClassifier<>(build(constraints, HugeMask.FACTORY.contiguous(maxPriority), HugeMask.FACTORY, maxPriority));
    }

    private <MaskType extends Mask<MaskType>>
    MaskedClassifier<MaskType, Input, Classification> build(List<MatchingConstraint<Key, Classification>> specs,
                                                            MaskType mask,
                                                            MaskFactory<MaskType> maskFactory,
                                                            int max) {
      PrimitiveIterator.OfInt sequence = IntStream.iterate(0, i -> i + 1).iterator();
      specs.stream().sorted(Comparator.comparingInt(rd -> order(rd.getPriority())))
                    .forEach(rule -> addMatchingConstraint(rule, sequence.nextInt(), maskFactory, max));
      return new MaskedClassifier<>(classifications, freezeMatchers(), mask);
    }

    private <MaskType extends Mask<MaskType>>
    void addMatchingConstraint(MatchingConstraint<Key, Classification> matchInfo,
                               int priority,
                               MaskFactory<MaskType> maskFactory,
                               int max) {
      classifications.add(matchInfo.getClassification());
      matchInfo.getConstraints().forEach((key, condition) -> memoisedMatcher(key, maskFactory, max).addConstraint(condition, priority));
    }

    private <MaskType extends Mask<MaskType>>
    MutableMatcher<Input, MaskType> memoisedMatcher(Key key, MaskFactory<MaskType> maskFactory, int max) {
      MutableMatcher<Input, MaskType> matcher = (MutableMatcher<Input, MaskType>)matchers.get(key);
      if (null == matcher) {
        matcher = registry.getAttribute(key).toMatcher(maskFactory, max);
        matchers.put(key, matcher);
      }
      return matcher;
    }

    private <MaskType extends Mask<MaskType>>
    List<Matcher<Input, MaskType>> freezeMatchers() {
      List<Matcher<Input, MaskType>> frozen = new ArrayList<>(matchers.size());
      for (MutableMatcher<Input, ? extends Mask> matcher : matchers.values()) {
        frozen.add((Matcher<Input, MaskType>) matcher.freeze());
      }
      return frozen;
    }

    private static int order(int priority) {
      return (1 << 16) - priority - 1;
    }
  }

}
