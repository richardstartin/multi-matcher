package com.openkappa.bitrules.nodes;

import com.openkappa.bitrules.Operation;
import org.junit.Test;
import org.roaringbitmap.ArrayContainer;
import org.roaringbitmap.Container;
import org.roaringbitmap.RunContainer;

import java.time.LocalDate;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ComparableNodeTest {

  private static final Container ZERO = new ArrayContainer().add((short) 0);
  private static final Container ONE = new ArrayContainer().add((short) 1);
  private static final Container ZERO_OR_ONE = ZERO.or(ONE);

  @Test
  public void testGreaterThan() {
    ComparableNode<LocalDate> node = build(100, Operation.GT);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.match(LocalDate.ofEpochDay(0), mask.clone()).isEmpty());
    assertEquals(ZERO, node.match(LocalDate.ofEpochDay(1), mask.clone()));
    assertEquals(ZERO_OR_ONE, node.match(LocalDate.ofEpochDay(11), mask.clone()));
  }

  @Test
  public void testEqual() {
    ComparableNode<LocalDate> node = build(100, Operation.EQ);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.match(LocalDate.ofEpochDay((1)), mask.clone()).isEmpty());
    assertEquals(ZERO, node.match(LocalDate.ofEpochDay(0), mask.clone()));
    assertEquals(ONE, node.match(LocalDate.ofEpochDay(10), mask.clone()));
  }

  @Test
  public void testLessThan() {
    ComparableNode<LocalDate> node = build(100, Operation.LT);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.match(LocalDate.ofEpochDay(1001), mask.clone()).isEmpty());
    assertEquals(mask.andNot(ZERO), node.match(LocalDate.ofEpochDay(0), mask.clone()));
    assertEquals(mask.andNot(ZERO_OR_ONE), node.match(LocalDate.ofEpochDay(10), mask.clone()));
  }

  @Test
  public void testGreaterThanRev() {
    ComparableNode<LocalDate> node = buildRev(100, Operation.GT);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.match(LocalDate.ofEpochDay(0), mask.clone()).isEmpty());
    assertEquals(ZERO, node.match(LocalDate.ofEpochDay(1), mask.clone()));
  }

  @Test
  public void testEqualRev() {
    ComparableNode<LocalDate> node = buildRev(100, Operation.EQ);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.match(LocalDate.ofEpochDay(1), mask.clone()).isEmpty());
    assertEquals(ZERO, node.match(LocalDate.ofEpochDay(0), mask.clone()));
    assertEquals(ONE, node.match(LocalDate.ofEpochDay(10), mask.clone()));
  }

  @Test
  public void testLessThanRev() {
    ComparableNode<LocalDate> node = buildRev(100, Operation.LT);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.match(LocalDate.ofEpochDay(1001), mask.clone()).isEmpty());
    assertEquals(mask.andNot(ZERO), node.match(LocalDate.ofEpochDay(0), mask.clone()));
    assertEquals(mask.andNot(ZERO_OR_ONE), node.match(LocalDate.ofEpochDay(10), mask.clone()));
  }

  @Test
  public void testBuildNode() {
    DoubleNode node = new DoubleNode(Operation.EQ);
    node.add(0, (short)0);
    assertEquals(RunContainer.rangeOfOnes(0, 1), node.match(0, RunContainer.rangeOfOnes(0, 1)));
    node.add(0, (short)1);
    assertEquals(RunContainer.rangeOfOnes(0, 2), node.match(0, RunContainer.rangeOfOnes(0, 2)));
  }

  private ComparableNode<LocalDate> build(int count, Operation operation) {
    ComparableNode<LocalDate> node = new ComparableNode<>(Comparator.<LocalDate>naturalOrder(), operation);
    for (int i = 0; i < count; ++i) {
      node.add(LocalDate.ofEpochDay(i * 10), (short) i);
    }
    return node.optimise();
  }

  private ComparableNode<LocalDate> buildRev(int count, Operation operation) {
    ComparableNode<LocalDate> node = new ComparableNode<>(Comparator.<LocalDate>naturalOrder(), operation);
    for (int i = count - 1; i >= 0; --i) {
      node.add(LocalDate.ofEpochDay(i * 10), (short) i);
    }
    return node.optimise();
  }
}