package uk.co.openkappa.bitrules.matchers;


import org.junit.jupiter.api.Test;
import uk.co.openkappa.bitrules.masks.SmallMask;
import uk.co.openkappa.bitrules.Operation;

import java.time.LocalDate;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.openkappa.bitrules.masks.SmallMask.FACTORY;
import static uk.co.openkappa.bitrules.Mask.with;

public class ComparableMutableNodeTest {

  private static final SmallMask ZERO = with(new SmallMask(), 0);
  private static final SmallMask ONE = with(new SmallMask(), 1);
  private static final SmallMask ZERO_OR_ONE = ZERO.or(ONE);

  @Test
  public void testGreaterThan() {
    ComparableMatcher.ComparableNode<LocalDate, SmallMask> node = build(100, Operation.GT);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.match(LocalDate.ofEpochDay(0), mask.clone()).isEmpty());
    assertEquals(ZERO, node.match(LocalDate.ofEpochDay(1), mask.clone()));
    assertEquals(ZERO_OR_ONE, node.match(LocalDate.ofEpochDay(11), mask.clone()));
  }

  @Test
  public void testEqual() {
    ComparableMatcher.ComparableNode<LocalDate, SmallMask> node = build(100, Operation.EQ);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.match(LocalDate.ofEpochDay((1)), mask.clone()).isEmpty());
    assertEquals(ZERO, node.match(LocalDate.ofEpochDay(0), mask.clone()));
    assertEquals(ONE, node.match(LocalDate.ofEpochDay(10), mask.clone()));
  }

  @Test
  public void testLessThan() {
    ComparableMatcher.ComparableNode<LocalDate, SmallMask> node = build(100, Operation.LT);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.match(LocalDate.ofEpochDay(1001), mask.clone()).isEmpty());
    assertEquals(mask.andNot(ZERO), node.match(LocalDate.ofEpochDay(0), mask.clone()));
    assertEquals(mask.andNot(ZERO_OR_ONE), node.match(LocalDate.ofEpochDay(10), mask.clone()));
  }

  @Test
  public void testGreaterThanRev() {
    ComparableMatcher.ComparableNode<LocalDate, SmallMask> node = buildRev(100, Operation.GT);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.match(LocalDate.ofEpochDay(0), mask.clone()).isEmpty());
    assertEquals(ZERO, node.match(LocalDate.ofEpochDay(1), mask.clone()));
  }

  @Test
  public void testEqualRev() {
    ComparableMatcher.ComparableNode<LocalDate, SmallMask> node = buildRev(100, Operation.EQ);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.match(LocalDate.ofEpochDay(1), mask.clone()).isEmpty());
    assertEquals(ZERO, node.match(LocalDate.ofEpochDay(0), mask.clone()));
    assertEquals(ONE, node.match(LocalDate.ofEpochDay(10), mask.clone()));
  }

  @Test
  public void testLessThanRev() {
    ComparableMatcher.ComparableNode<LocalDate, SmallMask> node = buildRev(100, Operation.LT);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.match(LocalDate.ofEpochDay(1001), mask.clone()).isEmpty());
    assertEquals(mask.andNot(ZERO), node.match(LocalDate.ofEpochDay(0), mask.clone()));
    assertEquals(mask.andNot(ZERO_OR_ONE), node.match(LocalDate.ofEpochDay(10), mask.clone()));
  }

  @Test
  public void testBuildNode() {
    ComparableMatcher.ComparableNode<Double, SmallMask> node = new ComparableMatcher.ComparableNode<>(Comparator.comparingDouble(Double::doubleValue), Operation.GT, new SmallMask());
    node.add(0D, 0);
    assertEquals(FACTORY.contiguous(1), node.match(1D, FACTORY.contiguous(1)));
    node.add(10D, 1);
    node.optimise();
    assertEquals(FACTORY.contiguous(2), node.match(11D, FACTORY.contiguous(2)));
  }

  private ComparableMatcher.ComparableNode<LocalDate, SmallMask> build(int count, Operation operation) {
    ComparableMatcher.ComparableNode<LocalDate, SmallMask> node = new ComparableMatcher.ComparableNode<>(Comparator.<LocalDate>naturalOrder(), operation, FACTORY.emptySingleton());
    for (int i = 0; i < count; ++i) {
      node.add(LocalDate.ofEpochDay(i * 10),  i);
    }
    return node.optimise();
  }

  private ComparableMatcher.ComparableNode<LocalDate, SmallMask> buildRev(int count, Operation operation) {
    ComparableMatcher.ComparableNode<LocalDate, SmallMask> node = new ComparableMatcher.ComparableNode<>(Comparator.<LocalDate>naturalOrder(), operation, FACTORY.emptySingleton());
    for (int i = count - 1; i >= 0; --i) {
      node.add(LocalDate.ofEpochDay(i * 10),  i);
    }
    return node.optimise();
  }
}