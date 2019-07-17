package uk.co.openkappa.bitrules;

import java.util.Optional;
import java.util.stream.Stream;

public class MaskedClassifier<MaskType extends Mask<MaskType>, Input, Classification> implements Classifier<Input, Classification> {

  private final Classification[] classifications;
  private final Matcher<Input, MaskType>[] rules;
  private final Mask<MaskType> mask;

  public MaskedClassifier(Classification[] classifications, Matcher<Input, MaskType>[] rules, Mask<MaskType> mask) {
    this.classifications = classifications;
    this.rules = rules;
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
    for (Matcher<Input, MaskType> matcher : rules) {
      context = matcher.match(value, context);
      if (context.isEmpty()) {
        break;
      }
    }
    return context;
  }
}
