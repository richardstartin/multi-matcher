package uk.co.openkappa.bitrules.matchers;


import org.junit.jupiter.api.Test;
import uk.co.openkappa.bitrules.masks.SmallMask;
import uk.co.openkappa.bitrules.Operation;
import uk.co.openkappa.bitrules.matchers.nodes.ComparableNode;

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
    ComparableNode<LocalDate, SmallMask> node = build(100, Operation.GT);
    assertTrue(node.match(LocalDate.ofEpochDay(0)).isEmpty());
    assertEquals(ZERO, node.match(LocalDate.ofEpochDay(1)));
    assertEquals(ZERO_OR_ONE, node.match(LocalDate.ofEpochDay(11)));
  }

  @Test
  public void testEqual() {
    ComparableNode<LocalDate, SmallMask> node = build(100, Operation.EQ);
    assertTrue(node.match(LocalDate.ofEpochDay((1))).isEmpty());
    assertEquals(ZERO, node.match(LocalDate.ofEpochDay(0)));
    assertEquals(ONE, node.match(LocalDate.ofEpochDay(10)));
  }

  @Test
  public void testLessThan() {
    ComparableNode<LocalDate, SmallMask> node = build(100, Operation.LT);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.match(LocalDate.ofEpochDay(1001)).isEmpty());
    assertEquals(mask.andNot(ZERO), node.match(LocalDate.ofEpochDay(0)));
    assertEquals(mask.andNot(ZERO_OR_ONE), node.match(LocalDate.ofEpochDay(10)));
  }

  @Test
  public void testGreaterThanRev() {
    ComparableNode<LocalDate, SmallMask> node = buildRev(100, Operation.GT);
    assertTrue(node.match(LocalDate.ofEpochDay(0)).isEmpty());
    assertEquals(ZERO, node.match(LocalDate.ofEpochDay(1)));
  }

  @Test
  public void testEqualRev() {
    ComparableNode<LocalDate, SmallMask> node = buildRev(100, Operation.EQ);
    assertTrue(node.match(LocalDate.ofEpochDay(1)).isEmpty());
    assertEquals(ZERO, node.match(LocalDate.ofEpochDay(0)));
    assertEquals(ONE, node.match(LocalDate.ofEpochDay(10)));
  }

  @Test
  public void testLessThanRev() {
    ComparableNode<LocalDate, SmallMask> node = buildRev(100, Operation.LT);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.match(LocalDate.ofEpochDay(1001)).isEmpty());
    assertEquals(mask.andNot(ZERO), node.match(LocalDate.ofEpochDay(0)));
    assertEquals(mask.andNot(ZERO_OR_ONE), node.match(LocalDate.ofEpochDay(10)));
  }

  @Test
  public void testBuildNode() {
    ComparableNode<Double, SmallMask> node = new ComparableNode<>(Comparator.comparingDouble(Double::doubleValue), Operation.GT, new SmallMask());
    node.add(0D, 0);
    assertEquals(FACTORY.contiguous(1), node.match(1D));
    node.add(10D, 1);
    node.freeze();
    assertEquals(FACTORY.contiguous(2), node.match(11D));
  }

  private ComparableNode<LocalDate, SmallMask> build(int count, Operation operation) {
    ComparableNode<LocalDate, SmallMask> node = new ComparableNode<>(Comparator.<LocalDate>naturalOrder(), operation, FACTORY.emptySingleton());
    for (int i = 0; i < count; ++i) {
      node.add(LocalDate.ofEpochDay(i * 10),  i);
    }
    return node.freeze();
  }

  private ComparableNode<LocalDate, SmallMask> buildRev(int count, Operation operation) {
    ComparableNode<LocalDate, SmallMask> node = new ComparableNode<>(Comparator.<LocalDate>naturalOrder(), operation, FACTORY.emptySingleton());
    for (int i = count - 1; i >= 0; --i) {
      node.add(LocalDate.ofEpochDay(i * 10),  i);
    }
    return node.freeze();
  }
}