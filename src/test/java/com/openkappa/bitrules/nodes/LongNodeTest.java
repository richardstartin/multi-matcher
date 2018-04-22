package com.openkappa.bitrules.nodes;

import com.openkappa.bitrules.LongRelation;
import org.junit.Test;
import org.roaringbitmap.ArrayContainer;
import org.roaringbitmap.Container;
import org.roaringbitmap.RunContainer;

import static org.junit.Assert.*;

public class LongNodeTest {


  @Test
  public void testGreaterThan() {
    LongNode node = build(100, LongRelation.GT);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(0L, mask.clone()).isEmpty());
    assertEquals(new ArrayContainer().add((short)0), node.apply(1L, mask.clone()));
  }


  @Test
  public void testGreaterThanOrEqual() {
    LongNode node = build(100, LongRelation.GE);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(-1L, mask.clone()).isEmpty());
    assertEquals(new ArrayContainer().add((short)0), node.apply(0L, mask.clone()));
    assertEquals(new ArrayContainer().add((short)0).add((short)1), node.apply(10L, mask.clone()));
  }

  @Test
  public void testEqual() {
    LongNode node = build(100, LongRelation.EQ);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(-1L, mask.clone()).isEmpty());
    assertEquals(new ArrayContainer().add((short)0), node.apply(0L, mask.clone()));
    assertEquals(new ArrayContainer().add((short)1), node.apply(10L, mask.clone()));
  }


  @Test
  public void testLessThanOrEqual() {
    LongNode node = build(100, LongRelation.LE);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask, node.apply(0L, mask.clone()));
    assertEquals(mask.remove((short)0), node.apply(10L, mask.clone()));
  }

  @Test
  public void testLessThan() {
    LongNode node = build(100, LongRelation.LT);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask.clone().remove((short)0), node.apply(0L, mask.clone()));
    assertEquals(mask.clone().remove((short)0).remove((short)1), node.apply(10L, mask.clone()));
  }

  @Test
  public void testGreaterThanRev() {
    LongNode node = buildRev(100, LongRelation.GT);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(0L, mask.clone()).isEmpty());
    assertEquals(new ArrayContainer().add((short)0), node.apply(1L, mask.clone()));
  }

  @Test
  public void testBuildNode() {
    LongNode node = new LongNode(LongRelation.EQ);
    node.add(0, (short)0);
    assertEquals(RunContainer.rangeOfOnes(0, 1), node.apply(0, RunContainer.rangeOfOnes(0, 1)));
    node.add(0, (short)1);
    assertEquals(RunContainer.rangeOfOnes(0, 2), node.apply(0, RunContainer.rangeOfOnes(0, 2)));
  }

  @Test
  public void testGreaterThanOrEqualRev() {
    LongNode node = buildRev(100, LongRelation.GE);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(-1L, mask.clone()).isEmpty());
    assertEquals(new ArrayContainer().add((short)0), node.apply(0L, mask.clone()));
    assertEquals(new ArrayContainer().add((short)0).add((short)1), node.apply(10L, mask.clone()));
  }

  @Test
  public void testEqualRev() {
    LongNode node = buildRev(100, LongRelation.EQ);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(-1L, mask.clone()).isEmpty());
    assertEquals(new ArrayContainer().add((short)0), node.apply(0L, mask.clone()));
    assertEquals(new ArrayContainer().add((short)1), node.apply(10L, mask.clone()));
  }


  @Test
  public void testLessThanOrEqualRev() {
    LongNode node = buildRev(100, LongRelation.LE);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask, node.apply(0L, mask.clone()));
    assertEquals(mask.remove((short)0), node.apply(10L, mask.clone()));
  }

  @Test
  public void testLessThanRev() {
    LongNode node = buildRev(100, LongRelation.LT);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask.clone().remove((short)0), node.apply(0L, mask.clone()));
    assertEquals(mask.clone().remove((short)0).remove((short)1), node.apply(10L, mask.clone()));
  }


  private LongNode build(int count, LongRelation relation) {
    LongNode node = new LongNode(relation);
    for (int i = 0; i < count; ++i) {
      node.add(i * 10, (short)i);
    }
    return node;
  }

  private LongNode buildRev(int count, LongRelation relation) {
    LongNode node = new LongNode(relation);
    for (int i = count - 1; i >= 0; --i) {
      node.add(i * 10, (short)i);
    }
    return node;
  }

}