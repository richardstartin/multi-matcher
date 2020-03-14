package io.github.richardstartin.multimatcher.core;


import io.github.richardstartin.multimatcher.core.masks.BitsetMask;
import io.github.richardstartin.multimatcher.core.masks.MaskFactory;
import io.github.richardstartin.multimatcher.core.masks.RoaringMask;
import io.github.richardstartin.multimatcher.core.masks.WordMask;
import io.github.richardstartin.multimatcher.core.schema.Schema;

import java.util.*;
import java.util.function.Consumer;

/**
 * Classifies objects according to constraints applied to
 * registered attributes.
 * @param <T> the classification input type
 * @param <C> the classification result type
 */
public interface Classifier<T, C> {


  /**
   * Visits all classifications matching the value
   * @param value the value to match
   * @param consumer the classification consumer
   */
  void forEachClassification(T value, Consumer<C> consumer);


  /**
   * Counts how many rules match the value
   * @param value the value to match
   * @return the number of matching rules
   */
  int matchCount(T value);

  /**
   * Gets the highest priority classification, or none if no constraints are satisfied.
   * @param value the value to classifications.
   * @return the best classification, or empty if no constraints are satisfied
   */
  Optional<C> classification(T value);


  /**
   * Gets the highest priority classification, or none if no constraints are satisfied.
   * @param value the value to classifications.
   * @return the best classification, or null if no constraints are satisfied
   */
  C classificationOrNull(T value);


  /**
   * Gets a new builder for a classifier
   *
   * @param <Key>            the create key type
   * @param <Input>          the type named the classified objects
   * @param <Classification> the classification type
   * @param schema the schema
   * @return a new classifier builder
   */
  static <Key, Input, Classification>
  ClassifierBuilder<Key, Input, Classification> builder(Schema<Key, Input> schema) {
    return new ClassifierBuilder<>(schema);
  }

  @SuppressWarnings("unchecked")
  class ClassifierBuilder<Key, Input, Classification> {

    private final Schema<Key, Input> schema;
    private final Map<Key, ConstraintAccumulator<Input, ? extends Mask<?>>> accumulators;
    private Classification[] classifications;
    private boolean useDirectBuffers = false;
    private int optimisedStorageSpace = 0;

    public ClassifierBuilder(Schema<Key, Input> schema) {
      this.schema = schema;
      this.accumulators = schema.newMap();
    }

    public ClassifierBuilder<Key, Input, Classification> useDirectBuffers(boolean useDirectBuffers) {
      this.useDirectBuffers = useDirectBuffers;
      return this;
    }

    public ClassifierBuilder<Key, Input, Classification> withOptimisedStorageSpace(int optimisedStorageSpace) {
      this.optimisedStorageSpace = optimisedStorageSpace;
      return this;
    }

    /**
     * Build a classifier from some matchers
     *
     * @param constraints the matching constraints
     * @return the classifier
     */
    public Classifier<Input, Classification> build(List<MatchingConstraint<Key, Classification>> constraints) {
      int maxPriority = constraints.size();
      if (maxPriority < WordMask.MAX_CAPACITY) {
        return build(constraints, WordMask.FACTORY, maxPriority);
      }
      if (maxPriority < BitsetMask.MAX_CAPACITY) {
        return build(constraints, BitsetMask.factory(maxPriority), maxPriority);
      }
      return build(constraints, RoaringMask.factory(optimisedStorageSpace, useDirectBuffers), maxPriority);
    }

    private <MaskType extends Mask<MaskType>>
    MaskedClassifier<MaskType, Input, Classification> build(List<MatchingConstraint<Key, Classification>> specs,
                                                            MaskFactory<MaskType> maskFactory,
                                                            int max) {
      classifications = (Classification[])new Object[max];
      int sequence = 0;
      specs.sort(Comparator.comparingInt(rd -> order(rd.getPriority())));
      for (var spec : specs) {
        addMatchingConstraint(spec, sequence++, maskFactory, max);
      }
      return new MaskedClassifier<>(classifications, freezeMatchers(), maskFactory.contiguous(max));
    }

    private <MaskType extends Mask<MaskType>>
    void addMatchingConstraint(MatchingConstraint<Key, Classification> matchInfo,
                               int priority,
                               MaskFactory<MaskType> maskFactory,
                               int max) {
      classifications[priority] = matchInfo.getClassification();
      for (var pair : matchInfo.getConstraints().entrySet()) {
        getOrCreateAccumulator(pair.getKey(), maskFactory, max)
                .addConstraint(pair.getValue(), priority);
      }
    }

    private <MaskType extends Mask<MaskType>>
    ConstraintAccumulator<Input, MaskType> getOrCreateAccumulator(Key key,
                                                                  MaskFactory<MaskType> maskFactory,
                                                                  int max) {
      var accumulator = (ConstraintAccumulator<Input, MaskType>) accumulators.get(key);
      if (null == accumulator) {
        accumulator = schema.getAttribute(key).newAccumulator(maskFactory, max);
        accumulators.put(key, accumulator);
      }
      return accumulator;
    }

    private <MaskType extends Mask<MaskType>>
    Matcher<Input, MaskType>[] freezeMatchers() {
      var matchers = new Matcher[accumulators.size()];
      int i = 0;
      for (var accumulator : accumulators.values()) {
        matchers[i++] = accumulator.toMatcher();
      }
      Arrays.sort(matchers, Comparator.comparingInt(x -> (int) (x.averageSelectivity() * 1000)));
      return (Matcher<Input, MaskType>[])matchers;
    }

    private static int order(int priority) {
      return (1 << 31) - priority - 1;
    }
  }

}
