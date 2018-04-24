package com.openkappa.bitrules.nodes;

import com.openkappa.bitrules.BiPredicateWithPriority;
import com.openkappa.bitrules.Constraint;
import com.openkappa.bitrules.Rule;
import org.roaringbitmap.Container;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DynamicRule<T, C> implements Rule<T> {

  private final Function<T, Optional<C>> dataContextProvider;
  private final List<BiPredicateWithPriority<T, Optional<C>>> predicates = new ArrayList<>();
  private final BiFunction<Constraint, Short, BiPredicateWithPriority<T, Optional<C>>> toPredicate;

  public DynamicRule(Function<T, Optional<C>> dataContextProvider,
                     BiFunction<Constraint, Short, BiPredicateWithPriority<T, Optional<C>>> toPredicate) {
    this.dataContextProvider = dataContextProvider;
    this.toPredicate = toPredicate;
  }

  @Override
  public Container match(T value, Container context) {
    Optional<C> dataContext = dataContextProvider.apply(value);
    Container ctx = context;
    for (BiPredicateWithPriority<T, Optional<C>> pred : predicates) {
      if (ctx.contains(pred.priority()) && !pred.test(value, dataContext)) {
        ctx = ctx.remove(pred.priority());
      }
    }
    return ctx;
  }

  @Override
  public void addConstraint(Constraint constraint, short priority) {
    predicates.add(toPredicate.apply(constraint, priority));
  }
}
