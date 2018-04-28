package uk.co.openkappa.bitrules.config;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public class ClassifierConfig<T> {

  private Map<String, Attribute<T>> rules = new HashMap<>();

  public static <U> ClassifierConfig<U> newInstance() {
    return new ClassifierConfig<>();
  }

  public <U> ClassifierConfig<T> withAttribute(String name, Function<T, U> accessor) {
    rules.put(name, new GenericAttribute<>(accessor));
    return this;
  }

  public <U> ClassifierConfig<T> withAttribute(String name, Function<T, U> accessor, Comparator<U> comparator) {
    rules.put(name, new ComparableAttribute<>(comparator, accessor));
    return this;
  }

  public ClassifierConfig<T> withAttribute(String name, ToDoubleFunction<T> accessor) {
    rules.put(name, new DoubleAttribute<>(accessor));
    return this;
  }

  public ClassifierConfig<T> withAttribute(String name, ToIntFunction<T> accessor) {
    rules.put(name, new IntAttribute<>(accessor));
    return this;
  }

  public ClassifierConfig<T> withAttribute(String name, ToLongFunction<T> accessor) {
    rules.put(name, new LongAttribute<>(accessor));
    return this;
  }

  public boolean hasAttribute(String name) {
    return rules.containsKey(name);
  }

  public Attribute<T> getAttribute(String name) {
    return rules.get(name);
  }

}
