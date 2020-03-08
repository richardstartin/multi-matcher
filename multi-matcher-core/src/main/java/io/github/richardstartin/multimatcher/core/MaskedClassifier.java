package io.github.richardstartin.multimatcher.core;

import io.github.richardstartin.multimatcher.core.masks.HugeMask;
import io.github.richardstartin.multimatcher.core.masks.MaskFactory;
import io.github.richardstartin.multimatcher.core.masks.SmallMask;
import io.github.richardstartin.multimatcher.core.masks.TinyMask;
import io.github.richardstartin.multimatcher.core.schema.Schema;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MaskedClassifier<MaskType extends Mask<MaskType>, Input, Classification> implements Classifier<Input, Classification> {

    private final Classification[] classifications;
    private final Matcher<Input, MaskType>[] matchers;
    private final Mask<MaskType> mask;

    public MaskedClassifier(Classification[] classifications, Matcher<Input, MaskType>[] matchers, Mask<MaskType> mask) {
        this.classifications = classifications;
        this.matchers = matchers;
        this.mask = mask;
    }

    @Override
    public Stream<Classification> classifications(Input value) {
        return match(value).stream().mapToObj(i -> classifications[i]);
    }

    @Override
    public Optional<Classification> classification(Input value) {
        MaskType matches = match(value);
        return matches.isEmpty()
                ? Optional.empty()
                : Optional.of(classifications[matches.first()]);
    }

    private MaskType match(Input value) {
        MaskType context = mask.clone();
        for (Matcher<Input, MaskType> matcher : matchers) {
            context = matcher.match(value, context);
            if (context.isEmpty()) {
                break;
            }
        }
        return context;
    }

    public static class ClassifierBuilder<Key, Input, Classification> {

        private final Schema<Key, Input> registry;
        private final Map<Key, ConstraintAccumulator<Input, ? extends Mask>> matchers = new HashMap<>();
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
        public Classifier<Input, Classification> build(List<MatchingConstraint<Key, Classification>> constraints) {
            int maxPriority = constraints.size();
            return maxPriority < TinyMask.MAX_CAPACITY
                    ? build(constraints, TinyMask.FACTORY, maxPriority)
                    : maxPriority < SmallMask.MAX_CAPACITY
                    ? build(constraints, SmallMask.FACTORY, maxPriority)
                    : build(constraints, HugeMask.FACTORY, maxPriority);
        }

        private <MaskType extends Mask<MaskType>>
        MaskedClassifier<MaskType, Input, Classification> build(List<MatchingConstraint<Key, Classification>> specs,
                                                                MaskFactory<MaskType> maskFactory,
                                                                int max) {
            PrimitiveIterator.OfInt sequence = IntStream.iterate(0, i -> i + 1).iterator();
            specs.stream().sorted(Comparator.comparingInt(rd -> order(rd.getPriority())))
                    .forEach(rule -> addMatchingConstraint(rule, sequence.nextInt(), maskFactory, max));
            return new MaskedClassifier<>((Classification[]) classifications.toArray(), freezeMatchers(), maskFactory.contiguous(max));
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
        ConstraintAccumulator<Input, MaskType> memoisedMatcher(Key key, MaskFactory<MaskType> maskFactory, int max) {
            ConstraintAccumulator<Input, MaskType> matcher = (ConstraintAccumulator<Input, MaskType>) matchers.get(key);
            if (null == matcher) {
                matcher = registry.getAttribute(key).toMatcher(maskFactory, max);
                matchers.put(key, matcher);
            }
            return matcher;
        }

        private <MaskType extends Mask<MaskType>>
        Matcher<Input, MaskType>[] freezeMatchers() {
            List<Matcher<Input, MaskType>> frozen = new ArrayList<>(matchers.size());
            for (ConstraintAccumulator<Input, ? extends Mask> matcher : matchers.values()) {
                frozen.add((Matcher<Input, MaskType>) matcher.freeze());
            }
            return frozen.stream()
                    .sorted(Comparator.comparingInt(x -> (int) (x.averageSelectivity() * 1000)))
                    .toArray(Matcher[]::new);
        }

        private static int order(int priority) {
            return (1 << 31) - priority - 1;
        }
    }
}
