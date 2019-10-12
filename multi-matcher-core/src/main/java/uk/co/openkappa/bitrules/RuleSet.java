package uk.co.openkappa.bitrules;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface RuleSet<Key, Classification> {

  List<MatchingConstraint<Key, Classification>> constraints() throws IOException;

  default Optional<MatchingConstraint<Key, Classification>> specification(String ruleId) throws IOException {
    return constraints().stream()
            .filter(rule -> rule.getId().equals(ruleId))
            .findFirst();
  }

}
