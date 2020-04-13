package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import io.github.richardstartin.multimatcher.core.masks.WordMask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.github.richardstartin.multimatcher.core.Constraint.equalTo;
import static io.github.richardstartin.multimatcher.core.Constraint.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Execution(ExecutionMode.CONCURRENT)
public class StringMutableMatcherTest {


    public static Stream<Arguments> stores() {
        return IntStream.of(32, 64)
                .mapToObj(i -> Arguments.of(i - 1, WordMask.store(i)));
    }

    @ParameterizedTest
    @MethodSource("stores")
    public void test1(int maxElement, MaskStore<WordMask> store) {
        StringConstraintAccumulator<String, WordMask> matcher = new StringConstraintAccumulator<>(Function.identity(), store, 4);
        matcher.addConstraint(equalTo("foo"), 0);
        matcher.addConstraint(equalTo("bar"), 1);
        matcher.addConstraint(startsWith("foo"), 2);
        matcher.addConstraint(startsWith("f"), 3);
        var mask = store.contiguous(maxElement);
        matcher.toMatcher().match("foo", mask);
        assertEquals(store.of(0, 2, 3), mask);
    }
}
