package com.openkappa.bitrules.config;


import com.openkappa.bitrules.BiPredicateWithPriority;
import com.openkappa.bitrules.Constraint;
import com.openkappa.bitrules.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.*;

public class ClassifierConfig<T> {

  private Map<String, ConstrainedAttribute<T>> rules = new HashMap<>();
  private Map<String, Function<Context, ToDoubleFunction<T>>> functions = new HashMap<>();

  public static <U> ClassifierConfig<U> newInstance() {
    return new ClassifierConfig<>();
  }

  public ClassifierConfig<T> withStringAttribute(String name, Function<T, String> accessor) {
    rules.put(name, new ConstrainedStringAttribute<>(accessor));
    return this;
  }

  public ClassifierConfig<T> withDoubleAttribute(String name, ToDoubleFunction<T> accessor) {
    rules.put(name, new ConstrainedDoubleAttribute<>(accessor));
    return this;
  }

  public ClassifierConfig<T> withIntAttribute(String name, ToIntFunction<T> accessor) {
    rules.put(name, new ConstrainedIntAttribute<>(accessor));
    return this;
  }

  public ClassifierConfig<T> withLongAttribute(String name, ToLongFunction<T> accessor) {
    rules.put(name, new ConstrainedLongAttribute<>(accessor));
    return this;
  }

  public ClassifierConfig<T> withContextualDoubleAttribute(String name, Function<Context, ToDoubleFunction<T>> factory) {
    functions.put(name, factory);
    return this;
  }

  public <C> ClassifierConfig<T> withDynamicAttribute(
          String name,
          Function<T, Optional<C>> dataContextProvider,
          BiFunction<Constraint, Short, BiPredicateWithPriority<T, Optional<C>>> predicateProvider) {
    rules.put(name, new DynamicallyConstrainedAttribute<>(dataContextProvider, predicateProvider));
    return this;
  }

  public boolean hasAttribute(String name) {
    return rules.containsKey(name);
  }

  public ConstrainedAttribute<T> getAttribute(String name) {
    return rules.get(name);
  }

  public boolean hasFunction(String name) {
    return functions.containsKey(name);
  }

  public Function<Context, ToDoubleFunction<T>> getFunction(String name) {
    return functions.get(name);
  }

}
