package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.masks.TinyMask;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static io.github.richardstartin.multimatcher.core.Constraint.equalTo;
import static io.github.richardstartin.multimatcher.core.Constraint.startsWith;
import static io.github.richardstartin.multimatcher.core.masks.TinyMask.FACTORY;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringMutableMatcherTest {

  @Test
  public void test1() {
    StringConstraintAccumulator<String, TinyMask> matcher = new StringConstraintAccumulator<>(Function.identity(), FACTORY, 4);
    matcher.addConstraint(equalTo("foo"), 0);
    matcher.addConstraint(equalTo("bar"), 1);
    matcher.addConstraint(startsWith("foo"), 2);
    matcher.addConstraint(startsWith("f"), 3);
    var mask = FACTORY.contiguous(63);
    matcher.freeze().match("foo", mask);
    assertEquals(FACTORY.of(0, 2, 3), mask);
  }
}
