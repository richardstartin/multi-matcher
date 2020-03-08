package io.github.richardstartin.multimatcher.core;

import java.util.Optional;
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
}
