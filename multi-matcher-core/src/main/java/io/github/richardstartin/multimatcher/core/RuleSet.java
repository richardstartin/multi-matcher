package io.github.richardstartin.multimatcher.core;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface RuleSet<Key, Classification> {

    List<MatchingConstraint<Key, Classification>> constraints() throws IOException;

    default Optional<MatchingConstraint<Key, Classification>> specification(String ruleId) throws IOException {
        for (var constraint : constraints()) {
            if (ruleId.equals(constraint.getId())) {
                return Optional.of(constraint);
            }
        }
        return Optional.empty();
    }

}
