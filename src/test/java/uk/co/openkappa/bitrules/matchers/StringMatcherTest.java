package uk.co.openkappa.bitrules.matchers;

import org.junit.jupiter.api.Test;
import uk.co.openkappa.bitrules.masks.TinyMask;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.co.openkappa.bitrules.Constraint.equalTo;
import static uk.co.openkappa.bitrules.Constraint.startsWith;
import static uk.co.openkappa.bitrules.masks.TinyMask.FACTORY;

public class StringMatcherTest {

  @Test
  public void test1() {
    StringMatcher<String, TinyMask> matcher = new StringMatcher<>(Function.identity(), FACTORY, 4);
    matcher.addConstraint(equalTo("foo"), 0);
    matcher.addConstraint(equalTo("bar"), 1);
    matcher.addConstraint(startsWith("foo"), 2);
    matcher.addConstraint(startsWith("f"), 3);
    matcher.freeze();
    TinyMask mask = matcher.match("foo", FACTORY.contiguous(63));
    assertEquals(FACTORY.of(0, 2, 3), mask);
  }
}
