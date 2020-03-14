package io.github.richardstartin.multimatcher.core;

import java.util.Optional;
import java.util.function.Consumer;

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
    public void forEachClassification(Input value, Consumer<Classification> consumer) {
        match(value).forEach(i -> consumer.accept(classifications[i]));
    }

    @Override
    public int matchCount(Input value) {
        return match(value).cardinality();
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
}
