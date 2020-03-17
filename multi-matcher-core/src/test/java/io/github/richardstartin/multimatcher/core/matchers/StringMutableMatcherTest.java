package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.masks.WordMask;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static io.github.richardstartin.multimatcher.core.Constraint.equalTo;
import static io.github.richardstartin.multimatcher.core.Constraint.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringMutableMatcherTest {

    @Test
    public void test1() {
        var store = WordMask.store();
        StringConstraintAccumulator<String, WordMask> matcher = new StringConstraintAccumulator<>(Function.identity(), store, 4);
        matcher.addConstraint(equalTo("foo"), 0);
        matcher.addConstraint(equalTo("bar"), 1);
        matcher.addConstraint(startsWith("foo"), 2);
        matcher.addConstraint(startsWith("f"), 3);
        var mask = store.contiguous(63);
        matcher.toMatcher().match("foo", mask);
        assertEquals(store.of(0, 2, 3), mask);
    }
}
