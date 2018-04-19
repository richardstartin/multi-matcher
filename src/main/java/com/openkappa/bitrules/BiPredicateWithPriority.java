package com.openkappa.bitrules;

import java.util.function.BiPredicate;

public interface BiPredicateWithPriority<T, C> extends BiPredicate<T, C> {
  short priority();
}
