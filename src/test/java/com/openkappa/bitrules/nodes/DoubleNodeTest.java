package com.openkappa.bitrules.nodes;

import com.openkappa.bitrules.DoubleRelation;
import org.junit.Test;
import org.roaringbitmap.ArrayContainer;
import org.roaringbitmap.Container;
import org.roaringbitmap.RunContainer;

import static org.junit.Assert.*;

public class DoubleNodeTest {

  private static final Container ZERO = new ArrayContainer().add((short) 0);
  private static final Container ONE = new ArrayContainer().add((short) 1);
  private static final Container ZERO_OR_ONE = ZERO.or(ONE);

  @Test
  public void testGreaterThan() {
    DoubleNode node = build(100, DoubleRelation.GT);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(0, mask.clone()).isEmpty());
    assertEquals(ZERO, node.apply(1, mask.clone()));
    assertEquals(ZERO_OR_ONE, node.apply(11, mask.clone()));
  }

  @Test
  public void testEqual() {
    DoubleNode node = build(100, DoubleRelation.EQ);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(ZERO, node.apply(0, mask.clone()));
    assertEquals(ONE, node.apply(10, mask.clone()));
  }

  @Test
  public void testLessThan() {
    DoubleNode node = build(100, DoubleRelation.LT);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask.andNot(ZERO), node.apply(0, mask.clone()));
    assertEquals(mask.andNot(ZERO_OR_ONE), node.apply(10, mask.clone()));
  }

  @Test
  public void testGreaterThanRev() {
    DoubleNode node = buildRev(100, DoubleRelation.GT);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(0, mask.clone()).isEmpty());
    assertEquals(ZERO, node.apply(1, mask.clone()));
  }

  @Test
  public void testEqualRev() {
    DoubleNode node = buildRev(100, DoubleRelation.EQ);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(ZERO, node.apply(0, mask.clone()));
    assertEquals(ONE, node.apply(10, mask.clone()));
  }

  @Test
  public void testLessThanRev() {
    DoubleNode node = buildRev(100, DoubleRelation.LT);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask.andNot(ZERO), node.apply(0, mask.clone()));
    assertEquals(mask.andNot(ZERO_OR_ONE), node.apply(10, mask.clone()));
  }

  @Test
  public void testBuildNode() {
    DoubleNode node = new DoubleNode(DoubleRelation.EQ);
    node.add(0, (short)0);
    assertEquals(RunContainer.rangeOfOnes(0, 1), node.apply(0, RunContainer.rangeOfOnes(0, 1)));
    node.add(0, (short)1);
    assertEquals(RunContainer.rangeOfOnes(0, 2), node.apply(0, RunContainer.rangeOfOnes(0, 2)));
  }

  private DoubleNode build(int count, DoubleRelation relation) {
    DoubleNode node = new DoubleNode(relation);
    for (int i = 0; i < count; ++i) {
      node.add(i * 10, (short) i);
    }
    return node.optimise();
  }

  private DoubleNode buildRev(int count, DoubleRelation relation) {
    DoubleNode node = new DoubleNode(relation);
    for (int i = count - 1; i >= 0; --i) {
      node.add(i * 10, (short) i);
    }
    return node.optimise();
  }
}