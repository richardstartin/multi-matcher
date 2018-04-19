package com.openkappa.bitrules.config;


import com.openkappa.bitrules.BiPredicateWithPriority;
import com.openkappa.bitrules.Constraint;
import com.openkappa.bitrules.Rule;
import com.openkappa.bitrules.nodes.DynamicRule;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DynamicallyConstrainedAttribute<T, C> implements ConstrainedAttribute<T> {

  private final Function<T, Optional<C>> dataContextProvider;
  private final BiFunction<Constraint, Short, BiPredicateWithPriority<T, Optional<C>>> predicateProvider;

  public DynamicallyConstrainedAttribute(Function<T, Optional<C>> dataContextProvider,
                                         BiFunction<Constraint, Short,
                                BiPredicateWithPriority<T, Optional<C>>> predicateProvider) {
    this.dataContextProvider = dataContextProvider;
    this.predicateProvider = predicateProvider;
  }

  @Override
  public Rule<T> toRule() {
    return new DynamicRule<>(dataContextProvider, predicateProvider);
  }
}
