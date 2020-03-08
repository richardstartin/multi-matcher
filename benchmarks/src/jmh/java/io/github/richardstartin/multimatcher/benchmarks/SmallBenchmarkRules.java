package io.github.richardstartin.multimatcher.benchmarks;

import io.github.richardstartin.multimatcher.core.MatchingConstraint;

import java.util.Arrays;
import java.util.List;

import static io.github.richardstartin.multimatcher.benchmarks.FieldsEnum.*;

public class SmallBenchmarkRules {

    public static DomainObject matching() {
        return new DomainObject("EUR", -1L, 25L, 10, 1.0, 0);
    }

    public static DomainObject nonMatching() {
        return new DomainObject("GBP", -1L, 5L, 10, 1.0, 0);
    }

    public static final List<MatchingConstraint<FieldsEnum, String>> ENUM_RULES = Arrays.asList(
            MatchingConstraint.<FieldsEnum, String>anonymous()
                    .eq(CURRENCY, "GBP")
                    .gt(TIMESTAMP, 10L)
                    .classification("c1")
                    .build(),
            MatchingConstraint.<FieldsEnum, String>anonymous()
                    .eq(CURRENCY, "EUR")
                    .gt(TIMESTAMP, 15L)
                    .classification("c2")
                    .build(),
            MatchingConstraint.<FieldsEnum, String>anonymous()
                    .eq(CURRENCY, "USD")
                    .gt(TIMESTAMP, 25L)
                    .gt(AMOUNT, 0)
                    .classification("c3")
                    .build()
    );

    public static final List<MatchingConstraint<String, String>> STRING_RULES = Arrays.asList(
            MatchingConstraint.<String, String>anonymous()
                    .eq(CURRENCY.name(), "GBP")
                    .gt(TIMESTAMP.name(), 10L)
                    .classification("c1")
                    .build(),
            MatchingConstraint.<String, String>anonymous()
                    .eq(CURRENCY.name(), "EUR")
                    .gt(TIMESTAMP.name(), 15L)
                    .classification("c2")
                    .build(),
            MatchingConstraint.<String, String>anonymous()
                    .eq(CURRENCY.name(), "USD")
                    .gt(TIMESTAMP.name(), 25L)
                    .gt(AMOUNT.name(), 0)
                    .classification("c3")
                    .build()
    );
}
