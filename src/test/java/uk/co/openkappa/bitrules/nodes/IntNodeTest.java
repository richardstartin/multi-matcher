package uk.co.openkappa.bitrules.nodes;

import uk.co.openkappa.bitrules.Operation;
import org.junit.Test;
import org.roaringbitmap.ArrayContainer;
import org.roaringbitmap.Container;
import org.roaringbitmap.RunContainer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IntNodeTest {

  @Test
  public void testGreaterThan() {
    IntNode node = build(100, Operation.GT);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(0, mask.clone()).isEmpty());
    assertEquals(new ArrayContainer().add((short) 0), node.apply(1, mask.clone()));
  }


  @Test
  public void testGreaterThanOrEqual() {
    IntNode node = build(100, Operation.GE);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(new ArrayContainer().add((short) 0), node.apply(0, mask.clone()));
    assertEquals(new ArrayContainer().add((short) 0).add((short) 1), node.apply(10, mask.clone()));
  }

  @Test
  public void testEqual() {
    IntNode node = build(100, Operation.EQ);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(new ArrayContainer().add((short) 0), node.apply(0, mask.clone()));
    assertEquals(new ArrayContainer().add((short) 1), node.apply(10, mask.clone()));
  }


  @Test
  public void testLessThanOrEqual() {
    IntNode node = build(100, Operation.LE);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask, node.apply(0, mask.clone()));
    assertEquals(mask.remove((short) 0), node.apply(10, mask.clone()));
  }

  @Test
  public void testLessThan() {
    IntNode node = build(100, Operation.LT);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask.clone().remove((short) 0), node.apply(0, mask.clone()));
    assertEquals(mask.clone().remove((short) 0).remove((short) 1), node.apply(10, mask.clone()));
  }

  @Test
  public void testGreaterThanRev() {
    IntNode node = buildRev(100, Operation.GT);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(0, mask.clone()).isEmpty());
    assertEquals(new ArrayContainer().add((short) 0), node.apply(1, mask.clone()));
  }


  @Test
  public void testGreaterThanOrEqualRev() {
    IntNode node = buildRev(100, Operation.GE);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(new ArrayContainer().add((short) 0), node.apply(0, mask.clone()));
    assertEquals(new ArrayContainer().add((short) 0).add((short) 1), node.apply(10, mask.clone()));
  }

  @Test
  public void testEqualRev() {
    IntNode node = buildRev(100, Operation.EQ);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(new ArrayContainer().add((short) 0), node.apply(0, mask.clone()));
    assertEquals(new ArrayContainer().add((short) 1), node.apply(10, mask.clone()));
  }


  @Test
  public void testLessThanOrEqualRev() {
    IntNode node = buildRev(100, Operation.LE);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask, node.apply(0, mask.clone()));
    assertEquals(mask.remove((short) 0), node.apply(10, mask.clone()));
  }

  @Test
  public void testLessThanRev() {
    IntNode node = buildRev(100, Operation.LT);
    Container mask = RunContainer.rangeOfOnes(0, 100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask.clone().remove((short) 0), node.apply(0, mask.clone()));
    assertEquals(mask.clone().remove((short) 0).remove((short) 1), node.apply(10, mask.clone()));
  }

  @Test
  public void testBuildNode() {
    IntNode node = new IntNode(Operation.EQ);
    node.add(0, (short)0);
    assertEquals(RunContainer.rangeOfOnes(0, 1), node.apply(0, RunContainer.rangeOfOnes(0, 1)));
    node.add(0, (short)1);
    assertEquals(RunContainer.rangeOfOnes(0, 2), node.apply(0, RunContainer.rangeOfOnes(0, 2)));
  }

  private IntNode build(int count, Operation relation) {
    IntNode node = new IntNode(relation);
    for (int i = 0; i < count; ++i) {
      node.add(i * 10, (short) i);
    }
    return node.optimise();
  }

  private IntNode buildRev(int count, Operation relation) {
    IntNode node = new IntNode(relation);
    for (int i = count - 1; i >= 0; --i) {
      node.add(i * 10, (short) i);
    }
    return node.optimise();
  }
}