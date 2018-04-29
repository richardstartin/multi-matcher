package uk.co.openkappa.bitrules;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface RuleSpecifications<Key, Classification> {

  List<RuleSpecification<Key, Classification>> get() throws IOException;

  default Optional<RuleSpecification<Key, Classification>> get(String ruleId) throws IOException {
    return get().stream()
            .filter(rule -> rule.getId().equals(ruleId))
            .findFirst();
  }

}
