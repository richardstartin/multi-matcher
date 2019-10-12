package uk.co.openkappa.bitrules.matchers;

import org.junit.jupiter.api.Test;
import uk.co.openkappa.bitrules.masks.SmallMask;
import uk.co.openkappa.bitrules.Operation;
import uk.co.openkappa.bitrules.matchers.nodes.IntNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.openkappa.bitrules.masks.SmallMask.FACTORY;
import static uk.co.openkappa.bitrules.Mask.with;
import static uk.co.openkappa.bitrules.Mask.without;

public class IntNodeTest {

  @Test
  public void testGreaterThan() {
    IntNode<SmallMask> node = build(100, Operation.GT);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(0, mask.clone()).isEmpty());
    assertEquals(with(new SmallMask(), 0), node.apply(1, mask.clone()));
  }


  @Test
  public void testGreaterThanOrEqual() {
    IntNode<SmallMask> node = build(100, Operation.GE);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(new SmallMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(with(new SmallMask(), 1), 0), node.apply(10, mask.clone()));
  }

  @Test
  public void testEqual() {
    IntNode<SmallMask> node = build(100, Operation.EQ);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(new SmallMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(new SmallMask(), 1), node.apply(10, mask.clone()));
  }


  @Test
  public void testLessThanOrEqual() {
    IntNode<SmallMask> node = build(100, Operation.LE);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask, node.apply(0, mask.clone()));
    assertEquals(without(mask.clone(), 0), node.apply(10, mask.clone()));
  }

  @Test
  public void testLessThan() {
    IntNode<SmallMask> node = build(100, Operation.LT);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(without(mask.clone(), 0), node.apply(0, mask.clone()));
    assertEquals(without(without(mask.clone(), 0), 1), node.apply(10, mask.clone()));
  }

  @Test
  public void testGreaterThanRev() {
    IntNode<SmallMask> node = buildRev(100, Operation.GT);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(0, mask.clone()).isEmpty());
    assertEquals(with(new SmallMask(), 0), node.apply(1, mask.clone()));
  }


  @Test
  public void testGreaterThanOrEqualRev() {
    IntNode<SmallMask> node = buildRev(100, Operation.GE);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(new SmallMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(with(new SmallMask(), 0), 1), node.apply(10, mask.clone()));
  }

  @Test
  public void testEqualRev() {
    IntNode<SmallMask> node = buildRev(100, Operation.EQ);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(new SmallMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(new SmallMask(), 1), node.apply(10, mask.clone()));
  }


  @Test
  public void testLessThanOrEqualRev() {
    IntNode<SmallMask> node = buildRev(100, Operation.LE);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask, node.apply(0, mask.clone()));
    assertEquals(without(mask.clone(), 0), node.apply(10, mask.clone()));
  }

  @Test
  public void testLessThanRev() {
    IntNode<SmallMask> node = buildRev(100, Operation.LT);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(without(mask.clone(), 0), node.apply(0, mask.clone()));
    assertEquals(without(without(mask.clone(), 0), 1), node.apply(10, mask.clone()));
  }

  @Test
  public void testBuildNode() {
    IntNode<SmallMask> node = new IntNode<>(Operation.EQ, new SmallMask());
    node.add(0, 0);
    assertEquals(FACTORY.contiguous( 1), node.apply(0, FACTORY.contiguous( 1)));
    node.add(0, 1);
    assertEquals(FACTORY.contiguous( 2), node.apply(0, FACTORY.contiguous( 2)));
  }

  private IntNode<SmallMask> build(int count, Operation relation) {
    IntNode<SmallMask> node = new IntNode<>(relation, new SmallMask());
    for (int i = 0; i < count; ++i) {
      node.add(i * 10, i);
    }
    return node.optimise();
  }

  private IntNode<SmallMask> buildRev(int count, Operation relation) {
    IntNode<SmallMask> node = new IntNode<>(relation, new SmallMask());
    for (int i = count - 1; i >= 0; --i) {
      node.add(i * 10, i);
    }
    return node.optimise();
  }
}