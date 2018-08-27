package uk.co.openkappa.bitrules;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class MaskedClassifier<MaskType extends Mask<MaskType>, Input, Classification> implements Classifier<Input, Classification> {

  private final List<Classification> classifications;
  private final Iterable<Matcher<Input, MaskType>> rules;
  private final Mask<MaskType> mask;

  public MaskedClassifier(List<Classification> classifications, Iterable<Matcher<Input, MaskType>> rules, Mask<MaskType> mask) {
    this.classifications = classifications;
    this.rules = rules;
    this.mask = mask;
  }

  @Override
  public Stream<Classification> classifications(Input value) {
    return match(value).stream().mapToObj(classifications::get);
  }

  @Override
  public Optional<Classification> classification(Input value) {
    MaskType matches = match(value);
    return matches.isEmpty()
            ? Optional.empty()
            : Optional.of(classifications.get(matches.first()));
  }

  private MaskType match(Input value) {
    MaskType context = mask.clone();
    Iterator<Matcher<Input, MaskType>> it = rules.iterator();
    while (it.hasNext() && !context.isEmpty()) {
      context = it.next().match(value, context);
    }
    return context;
  }
}
