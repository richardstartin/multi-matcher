package com.openkappa.bitrules.config;

import com.openkappa.bitrules.Rule;

public interface ConstrainedAttribute<T> {
  Rule<T> toRule();
}
