package uk.co.openkappa.bitrules.config;

import uk.co.openkappa.bitrules.Rule;

public interface Attribute<T> {
  Rule<T> toRule();
}
