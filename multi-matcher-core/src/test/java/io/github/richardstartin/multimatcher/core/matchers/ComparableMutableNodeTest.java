package io.github.richardstartin.multimatcher.core.matchers;


import io.github.richardstartin.multimatcher.core.Operation;
import io.github.richardstartin.multimatcher.core.masks.BitmapMask;
import io.github.richardstartin.multimatcher.core.matchers.nodes.ComparableNode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Comparator;

import static io.github.richardstartin.multimatcher.core.Mask.with;
import static io.github.richardstartin.multimatcher.core.masks.BitmapMask.factory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComparableMutableNodeTest {

  private static BitmapMask.Factory FACTORY = factory(200);

  private static final BitmapMask ZERO = with(FACTORY.empty(), 0);
  private static final BitmapMask ONE = with(FACTORY.empty(), 1);
  private static final BitmapMask ZERO_OR_ONE = ZERO.or(ONE);

  @Test
  public void testGreaterThan() {
    ComparableNode<LocalDate, BitmapMask> node = build(100, Operation.GT);
    assertTrue(node.match(LocalDate.ofEpochDay(0)).isEmpty());
    assertEquals(ZERO, node.match(LocalDate.ofEpochDay(1)));
    assertEquals(ZERO_OR_ONE, node.match(LocalDate.ofEpochDay(11)));
  }

  @Test
  public void testEqual() {
    ComparableNode<LocalDate, BitmapMask> node = build(100, Operation.EQ);
    assertTrue(node.match(LocalDate.ofEpochDay((1))).isEmpty());
    assertEquals(ZERO, node.match(LocalDate.ofEpochDay(0)));
    assertEquals(ONE, node.match(LocalDate.ofEpochDay(10)));
  }

  @Test
  public void testLessThan() {
    ComparableNode<LocalDate, BitmapMask> node = build(100, Operation.LT);
    BitmapMask mask = FACTORY.contiguous(100);
    assertTrue(node.match(LocalDate.ofEpochDay(1001)).isEmpty());
    assertEquals(mask.andNot(ZERO), node.match(LocalDate.ofEpochDay(0)));
    assertEquals(mask.andNot(ZERO_OR_ONE), node.match(LocalDate.ofEpochDay(10)));
  }

  @Test
  public void testGreaterThanRev() {
    ComparableNode<LocalDate, BitmapMask> node = buildRev(100, Operation.GT);
    assertTrue(node.match(LocalDate.ofEpochDay(0)).isEmpty());
    assertEquals(ZERO, node.match(LocalDate.ofEpochDay(1)));
  }

  @Test
  public void testEqualRev() {
    ComparableNode<LocalDate, BitmapMask> node = buildRev(100, Operation.EQ);
    assertTrue(node.match(LocalDate.ofEpochDay(1)).isEmpty());
    assertEquals(ZERO, node.match(LocalDate.ofEpochDay(0)));
    assertEquals(ONE, node.match(LocalDate.ofEpochDay(10)));
  }

  @Test
  public void testLessThanRev() {
    ComparableNode<LocalDate, BitmapMask> node = buildRev(100, Operation.LT);
    BitmapMask mask = FACTORY.contiguous(100);
    assertTrue(node.match(LocalDate.ofEpochDay(1001)).isEmpty());
    assertEquals(mask.andNot(ZERO), node.match(LocalDate.ofEpochDay(0)));
    assertEquals(mask.andNot(ZERO_OR_ONE), node.match(LocalDate.ofEpochDay(10)));
  }

  @Test
  public void testBuildNode() {
    ComparableNode<Double, BitmapMask> node = new ComparableNode<>(Comparator.comparingDouble(Double::doubleValue), Operation.GT, FACTORY.empty());
    node.add(0D, 0);
    assertEquals(FACTORY.contiguous(1), node.match(1D));
    node.add(10D, 1);
    node.freeze();
    assertEquals(FACTORY.contiguous(2), node.match(11D));
  }

  private ComparableNode<LocalDate, BitmapMask> build(int count, Operation operation) {
    ComparableNode<LocalDate, BitmapMask> node = new ComparableNode<>(Comparator.<LocalDate>naturalOrder(), operation, FACTORY.emptySingleton());
    for (int i = 0; i < count; ++i) {
      node.add(LocalDate.ofEpochDay(i * 10),  i);
    }
    return node.freeze();
  }

  private ComparableNode<LocalDate, BitmapMask> buildRev(int count, Operation operation) {
    ComparableNode<LocalDate, BitmapMask> node = new ComparableNode<>(Comparator.<LocalDate>naturalOrder(), operation, FACTORY.emptySingleton());
    for (int i = count - 1; i >= 0; --i) {
      node.add(LocalDate.ofEpochDay(i * 10),  i);
    }
    return node.freeze();
  }
}