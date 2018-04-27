package com.openkappa.bitrules.config;

import com.openkappa.bitrules.Rule;

public interface Attribute<T> {
  Rule<T> toRule();
}
