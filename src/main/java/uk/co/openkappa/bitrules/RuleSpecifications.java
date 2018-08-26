package uk.co.openkappa.bitrules;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface RuleSpecifications<Key, Classification> {

  List<MatchingConstraint<Key, Classification>> specifications() throws IOException;

  default Optional<MatchingConstraint<Key, Classification>> specification(String ruleId) throws IOException {
    return specifications().stream()
            .filter(rule -> rule.getId().equals(ruleId))
            .findFirst();
  }

}
