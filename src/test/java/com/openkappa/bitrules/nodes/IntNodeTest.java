package com.openkappa.bitrules.nodes;

import com.openkappa.bitrules.IntRelation;
import org.junit.Test;
import org.roaringbitmap.ArrayContainer;
import org.roaringbitmap.Container;
import org.roaringbitmap.RunContainer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IntNodeTest {

  @Test
  public void testGreaterThan() {
    IntNode node = build(100, IntRelation.GT);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(0, mask.clone()).isEmpty());
    assertEquals(new ArrayContainer().add((short) 0), node.apply(1, mask.clone()));
  }


  @Test
  public void testGreaterThanOrEqual() {
    IntNode node = build(100, IntRelation.GE);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(new ArrayContainer().add((short) 0), node.apply(0, mask.clone()));
    assertEquals(new ArrayContainer().add((short) 0).add((short) 1), node.apply(10, mask.clone()));
  }

  @Test
  public void testEqual() {
    IntNode node = build(100, IntRelation.EQ);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(new ArrayContainer().add((short) 0), node.apply(0, mask.clone()));
    assertEquals(new ArrayContainer().add((short) 1), node.apply(10, mask.clone()));
  }


  @Test
  public void testLessThanOrEqual() {
    IntNode node = build(100, IntRelation.LE);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask, node.apply(0, mask.clone()));
    assertEquals(mask.remove((short) 0), node.apply(10, mask.clone()));
  }

  @Test
  public void testLessThan() {
    IntNode node = build(100, IntRelation.LT);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask.clone().remove((short) 0), node.apply(0, mask.clone()));
    assertEquals(mask.clone().remove((short) 0).remove((short) 1), node.apply(10, mask.clone()));
  }

  private IntNode build(int count, IntRelation relation) {
    IntNode node = new IntNode(relation);
    for (int i = 0; i < count; ++i) {
      node.add(i * 10, (short) i);
    }
    return node;
  }
}