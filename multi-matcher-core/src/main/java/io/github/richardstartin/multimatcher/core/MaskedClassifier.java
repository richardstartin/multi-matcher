package io.github.richardstartin.multimatcher.core;

import io.github.richardstartin.multimatcher.core.masks.*;
import io.github.richardstartin.multimatcher.core.schema.Schema;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MaskedClassifier<MaskType extends Mask<MaskType>, Input, Classification>
        implements Classifier<Input, Classification> {

    private final Classification[] classifications;
    private final Matcher<Input, MaskType>[] matchers;
    private final Mask<MaskType> mask;
    private final ThreadLocal<MaskType> context;

    public MaskedClassifier(Classification[] classifications,
                            Matcher<Input, MaskType>[] matchers,
                            Mask<MaskType> mask) {
        this.classifications = classifications;
        this.matchers = matchers;
        this.mask = mask;
        this.context = ThreadLocal.withInitial(mask::clone);
        mask.optimise();
    }

    @Override
    public Stream<Classification> classifications(Input value) {
        return match(value).stream().mapToObj(i -> classifications[i]);
    }

    @Override
    public Optional<Classification> classification(Input value) {
        return Optional.ofNullable(classificationOrNull(value));
    }

    @Override
    public Classification classificationOrNull(Input value) {
        var matches = match(value);
        return matches.isEmpty()
                ? null
                : classifications[matches.first()];
    }

    private MaskType match(Input value) {
        var ctx = context.get().resetTo(mask);
        for (var matcher : matchers) {
            matcher.match(value, ctx);
            if (ctx.isEmpty()) {
                break;
            }
        }
        return ctx;
    }

    @SuppressWarnings("unchecked")
    public static class ClassifierBuilder<Key, Input, Classification> {

        private final Schema<Key, Input> registry;
        private final Map<Key, ConstraintAccumulator<Input, ? extends Mask<?>>> accumulators = new HashMap<>();
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
            if (maxPriority < TinyMask.MAX_CAPACITY) {
                return build(constraints, TinyMask.FACTORY, maxPriority);
            }
            if (maxPriority < BitmapMask.MAX_CAPACITY) {
                return build(constraints, BitmapMask.factory(maxPriority), maxPriority);
            }
            return build(constraints, RoaringMask.FACTORY, maxPriority);
        }

        private <MaskType extends Mask<MaskType>>
        MaskedClassifier<MaskType, Input, Classification> build(List<MatchingConstraint<Key, Classification>> specs,
                                                                MaskFactory<MaskType> maskFactory,
                                                                int max) {
            var sequence = IntStream.iterate(0, i -> i + 1).iterator();
            specs.sort(Comparator.comparingInt(rd -> order(rd.getPriority())));
            for (var spec : specs) {
                addMatchingConstraint(spec, sequence.nextInt(), maskFactory, max);
            }
            return new MaskedClassifier<>((Classification[]) classifications.toArray(), freezeMatchers(),
                    maskFactory.contiguous(max));
        }

        private <MaskType extends Mask<MaskType>>
        void addMatchingConstraint(MatchingConstraint<Key, Classification> matchInfo,
                                   int priority,
                                   MaskFactory<MaskType> maskFactory,
                                   int max) {
            classifications.add(matchInfo.getClassification());
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
                accumulator = registry.getAttribute(key).newAccumulator(maskFactory, max);
                accumulators.put(key, accumulator);
            }
            return accumulator;
        }

        private <MaskType extends Mask<MaskType>>
        Matcher<Input, MaskType>[] freezeMatchers() {
            var frozen = new Matcher[accumulators.size()];
            int i = 0;
            for (var matcher : accumulators.values()) {
                frozen[i++] = matcher.freeze();
            }
            Arrays.sort(frozen, Comparator.comparingInt(x -> (int) (x.averageSelectivity() * 1000)));
            return (Matcher<Input, MaskType>[])frozen;
        }

        private static int order(int priority) {
            return (1 << 31) - priority - 1;
        }
    }
}
