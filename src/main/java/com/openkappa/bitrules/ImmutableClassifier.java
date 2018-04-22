package com.openkappa.bitrules;


import com.openkappa.bitrules.config.FunctionProvider;
import com.openkappa.bitrules.config.ClassifierConfig;
import com.openkappa.bitrules.config.RuleAttributeNotRegistered;
import com.openkappa.bitrules.nodes.NamedFunctionRule;
import com.openkappa.bitrules.source.RuleSpecifications;
import org.roaringbitmap.Container;
import org.roaringbitmap.RunContainer;
import org.roaringbitmap.ShortIterator;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ImmutableClassifier<T> implements Classifier<T> {

  private final List<String> classifications;
  private final Collection<Rule<T>> rules;
  private final Container mask;

  ImmutableClassifier(List<String> classifications, Collection<Rule<T>> rules, short max) {
    this.classifications = classifications;
    this.rules = rules;
    this.mask = new RunContainer().add(0, max);
  }

  public static <U> ClassifierBuilder<U> builder() {
    return new ClassifierBuilder<>();
  }

  @Override
  public Stream<String> classify(T value) {
    Container matches = match(value);
    ShortIterator it = matches.getReverseShortIterator();
    return IntStream.generate(it::nextAsInt)
            .limit(matches.getCardinality())
            .mapToObj(classifications::get);
  }

  @Override
  public Optional<String> getBestClassification(T value) {
    Container container = match(value);
    return container.isEmpty()
            ? Optional.empty()
            : Optional.of(classifications.get(container.last()));
  }

  private Container match(T value) {
    Container context = mask.clone();
    Iterator<Rule<T>> it = rules.iterator();
    while (it.hasNext() && !context.isEmpty()) {
      context = it.next().apply(value, context);
    }
    return context;
  }

  public static class ClassifierBuilder<T> {

    private static ClassifierConfig<?> DEFAULT = new ClassifierConfig();

    private ClassifierConfig<T> config = (ClassifierConfig<T>) DEFAULT;
    private Map<String, Rule<T>> rules = new HashMap<>();
    private List<String> classifications = new ArrayList<>();

    private short maxPriority = 0;

    public ClassifierBuilder<T> withConfig(ClassifierConfig<T> config) {
      this.config = config;
      return this;
    }

    public ImmutableClassifier<T> build(RuleSpecifications repository) throws IOException {
      buildFromRepository(repository);
      return new ImmutableClassifier<>(classifications, rules.values(), maxPriority);
    }

    private void buildFromRepository(RuleSpecifications source) throws IOException {
      PrimitiveIterator.OfInt seq = IntStream.iterate(0, i -> i + 1).iterator();
      source.get()
            .stream()
            .sorted(Comparator.comparingInt(rd -> rd.getPriority() & 0xFFFF))
            .forEach(rule -> addRuleData(rule, (short) seq.nextInt()));
      maxPriority = (short) seq.nextInt();
    }

    private void addRuleData(RuleSpecification ruleInfo, short salience) {
      classifications.add(ruleInfo.getClassification());
      for (Map.Entry<String, Constraint> condition : ruleInfo.getConstraints().entrySet()) {
        Constraint rc = condition.getValue();
        String key = isNullOrEmpty(rc.getFunction()) ? condition.getKey() : rc.getFunction();
        final Rule<T> rule;
        if (rules.containsKey(key)) {
          rule = rules.get(key);
        } else if (config.hasAttribute(key)) {
          rule = config.getAttribute(key).toRule();
          rules.put(key, rule);
        } else if (config.hasFunction(key)) {
          Function<Context, ToDoubleFunction<T>> factory = config.getFunction(key);
          rule = new NamedFunctionRule<>(FunctionProvider.of(factory));
          rules.put(key, rule);
        } else {
          throw new RuleAttributeNotRegistered("No attribute or function [" + key + "] registered.");
        }
        rule.addConstraint(condition.getValue(), salience);
      }
    }

    private static boolean isNullOrEmpty(String s) {
      return null == s || s.length() == 0;
    }
  }

}
