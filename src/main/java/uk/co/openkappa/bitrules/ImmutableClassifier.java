package uk.co.openkappa.bitrules;


import org.roaringbitmap.Container;
import org.roaringbitmap.RunContainer;
import org.roaringbitmap.ShortIterator;
import uk.co.openkappa.bitrules.config.Schema;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Immutable classifier. A new instance must be built if rules are updated.
 *
 * @param <Input> the type of the classified objects
 * @param <Input> the type of the resultant classification
 */
public class ImmutableClassifier<Input, Classification> implements Classifier<Input, Classification> {

  private final List<Classification> classifications;
  private final Collection<Rule<Input>> rules;
  private final Container mask;

  ImmutableClassifier(List<Classification> classifications, Collection<Rule<Input>> rules, int max) {
    this.classifications = classifications;
    this.rules = rules;
    this.mask = new RunContainer().add(0, max);
    rules.forEach(Rule::freeze);
  }

  /**
   * Gets a new builder for a classifier
   *
   * @param <Key>            the schema key type
   * @param <Input>          the type of the classified objects
   * @param <Classification> the classification type
   * @return a new builder
   */
  public static <Key, Input, Classification> ClassifierBuilder<Key, Input, Classification> definedBy(
          Schema<Key, Input> schema) {
    return new ClassifierBuilder<>(schema);
  }

  @Override
  public Stream<Classification> classify(Input value) {
    Container matches = match(value);
    ShortIterator it = matches.getShortIterator();
    return IntStream.generate(it::nextAsInt)
            .limit(matches.getCardinality())
            .mapToObj(classifications::get);
  }

  @Override
  public Optional<Classification> getBestClassification(Input value) {
    Container container = match(value);
    return container.isEmpty()
            ? Optional.empty()
            : Optional.of(classifications.get(container.first()));
  }

  private Container match(Input value) {
    Container context = mask.clone();
    Iterator<Rule<Input>> it = rules.iterator();
    while (it.hasNext() && !context.isEmpty()) {
      context = it.next().match(value, context);
    }
    return context;
  }

  public static class ClassifierBuilder<Key, Input, Classification> {

    private final Schema<Key, Input> registry;
    private Map<Key, Rule<Input>> rules = new HashMap<>();
    private List<Classification> classifications = new ArrayList<>();

    private int maxPriority = 0;

    public ClassifierBuilder(Schema<Key, Input> registry) {
      this.registry = registry;
    }

    /**
     * Build a classifier from some rules
     *
     * @param repository the container of rules
     * @return the classifier built from the current snapshot of the repository and attribute registry
     * @throws IOException if the repository throws
     */
    public ImmutableClassifier<Input, Classification> build(RuleSpecifications<Key, Classification> repository) throws IOException {
      PrimitiveIterator.OfInt sequence = IntStream.iterate(0, i -> i + 1).iterator();
      repository.get()
              .stream()
              .sorted(Comparator.comparingInt(rd -> (1 << 16) - rd.getPriority() - 1))
              .forEach(rule -> addRuleData(rule, (short) sequence.nextInt()));
      maxPriority = sequence.nextInt();
      return new ImmutableClassifier<>(classifications, rules.values(), maxPriority);
    }

    private void addRuleData(RuleSpecification<Key, Classification> ruleInfo, short priority) {
      classifications.add(ruleInfo.getClassification());
      ruleInfo.getConstraints()
              .forEach((key, condition) -> memoisedRuleFor(key).addConstraint(condition, priority));
    }

    private Rule<Input> memoisedRuleFor(Key key) {
      if (!rules.containsKey(key)) {
        rules.put(key, registry.getAttribute(key).toRule());
      }
      return rules.get(key);
    }
  }

}
