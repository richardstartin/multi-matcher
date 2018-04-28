package uk.co.openkappa.bitrules;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface RuleSpecifications {

  List<RuleSpecification> get() throws IOException;

  default Optional<RuleSpecification> get(String ruleId) throws IOException {
    return get().stream()
            .filter(rule -> rule.getId().equals(ruleId))
            .findFirst();
  }

}
