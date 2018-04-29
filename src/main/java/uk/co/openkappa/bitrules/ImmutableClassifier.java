package uk.co.openkappa.bitrules;


import uk.co.openkappa.bitrules.config.AttributeRegistry;
import uk.co.openkappa.bitrules.config.RuleAttributeNotRegistered;
import org.roaringbitmap.Container;
import org.roaringbitmap.RunContainer;
import org.roaringbitmap.ShortIterator;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Immutable classifier. A new instance must be built if rules are updated.
 * @param <Input> the type of the classified objects
 * @param <Input> the type of the resultant classification
 */
public class ImmutableClassifier<Input, Classification> implements Classifier<Input, Classification> {

  private final List<Classification> classifications;
  private final Collection<Rule<Input>> rules;
  private final Container mask;

  ImmutableClassifier(List<Classification> classifications, Collection<Rule<Input>> rules, short max) {
    this.classifications = classifications;
    this.rules = rules;
    this.mask = new RunContainer().add(0, max);
    rules.forEach(Rule::freeze);
  }

  /**
   * Gets a new builder for a classifier
   * @param <U> the type of the clasified objects
   * @return a new builder
   */
  public static <U, Classification> ClassifierBuilder<U, Classification> forSchema(
          AttributeRegistry<U> schema) {
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

  public static class ClassifierBuilder<Input, Classification> {

    private final AttributeRegistry<Input> registry;
    private Map<String, Rule<Input>> rules = new HashMap<>();
    private List<Classification> classifications = new ArrayList<>();

    private short maxPriority = 0;

    public ClassifierBuilder(AttributeRegistry<Input> registry) {
      this.registry = registry;
    }

    /**
     * Build a classifier from some rules
     * @param repository the container of rules
     * @return the classifier built from the current snapshot of the repository and attribute registry
     * @throws IOException if the repository throws
     */
    public ImmutableClassifier<Input, Classification> build(RuleSpecifications repository) throws IOException {
      PrimitiveIterator.OfInt sequence = IntStream.iterate(0, i -> i + 1).iterator();
      repository.get()
              .stream()
              .sorted(Comparator.comparingInt(rd -> (1 << 16) - rd.getPriority() - 1))
              .forEach(rule -> addRuleData(rule, (short) sequence.nextInt()));
      maxPriority = (short) sequence.nextInt();
      return new ImmutableClassifier<>(classifications, rules.values(), maxPriority);
    }

    private void addRuleData(RuleSpecification<Classification> ruleInfo, short priority) {
      classifications.add(ruleInfo.getClassification());
      for (Map.Entry<String, Constraint> condition : ruleInfo.getConstraints().entrySet()) {
        String key = condition.getKey();
        final Rule<Input> rule;
        if (rules.containsKey(key)) {
          rule = rules.get(key);
        } else if (registry.hasAttribute(key)) {
          rule = registry.getAttribute(key).toRule();
          rules.put(key, rule);
        } else {
          throw new RuleAttributeNotRegistered("No attribute " + key + " registered.");
        }
        rule.addConstraint(condition.getValue(), priority);
      }
    }
  }

}
