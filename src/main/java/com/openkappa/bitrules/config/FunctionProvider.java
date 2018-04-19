package com.openkappa.bitrules.config;


import com.openkappa.bitrules.Context;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public class FunctionProvider<T> implements Function<Context, ToDoubleFunction<T>> {

  private final Function<Context, ToDoubleFunction<T>> factory;

  public FunctionProvider(Function<Context, ToDoubleFunction<T>> factory) {
    this.factory = factory;
  }

  public static <T> FunctionProvider<T> of(Function<Context, ToDoubleFunction<T>> factory) {
    return new FunctionProvider<>(factory);
  }

  @Override
  public ToDoubleFunction<T> apply(Context context) {
    return factory.apply(context);
  }
}
