package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.Operation;
import io.github.richardstartin.multimatcher.core.masks.BitmapMask;
import io.github.richardstartin.multimatcher.core.matchers.nodes.IntNode;
import org.junit.jupiter.api.Test;

import static io.github.richardstartin.multimatcher.core.Mask.with;
import static io.github.richardstartin.multimatcher.core.Mask.without;
import static io.github.richardstartin.multimatcher.core.masks.BitmapMask.factory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntNodeTest {
  
  private static BitmapMask.Factory factory = factory(200);

  @Test
  public void testGreaterThan() {
    IntNode<BitmapMask> node = build(100, Operation.GT);
    BitmapMask mask = factory.contiguous(100);
    assertTrue(node.apply(0, mask.clone()).isEmpty());
    assertEquals(with(factory.newMask(), 0), node.apply(1, mask.clone()));
  }


  @Test
  public void testGreaterThanOrEqual() {
    IntNode<BitmapMask> node = build(100, Operation.GE);
    BitmapMask mask = factory.contiguous(100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(factory.newMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(with(factory.newMask(), 1), 0), node.apply(10, mask.clone()));
  }

  @Test
  public void testEqual() {
    IntNode<BitmapMask> node = build(100, Operation.EQ);
    BitmapMask mask = factory.contiguous(100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(factory.newMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(factory.newMask(), 1), node.apply(10, mask.clone()));
  }


  @Test
  public void testLessThanOrEqual() {
    IntNode<BitmapMask> node = build(100, Operation.LE);
    BitmapMask mask = factory.contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask, node.apply(0, mask.clone()));
    assertEquals(without(mask.clone(), 0), node.apply(10, mask.clone()));
  }

  @Test
  public void testLessThan() {
    IntNode<BitmapMask> node = build(100, Operation.LT);
    BitmapMask mask = factory.contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(without(mask.clone(), 0), node.apply(0, mask.clone()));
    assertEquals(without(without(mask.clone(), 0), 1), node.apply(10, mask.clone()));
  }

  @Test
  public void testGreaterThanRev() {
    IntNode<BitmapMask> node = buildRev(100, Operation.GT);
    BitmapMask mask = factory.contiguous(100);
    assertTrue(node.apply(0, mask.clone()).isEmpty());
    assertEquals(with(factory.newMask(), 0), node.apply(1, mask.clone()));
  }


  @Test
  public void testGreaterThanOrEqualRev() {
    IntNode<BitmapMask> node = buildRev(100, Operation.GE);
    BitmapMask mask = factory.contiguous(100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(factory.newMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(with(factory.newMask(), 0), 1), node.apply(10, mask.clone()));
  }

  @Test
  public void testEqualRev() {
    IntNode<BitmapMask> node = buildRev(100, Operation.EQ);
    BitmapMask mask = factory.contiguous(100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(factory.newMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(factory.newMask(), 1), node.apply(10, mask.clone()));
  }


  @Test
  public void testLessThanOrEqualRev() {
    IntNode<BitmapMask> node = buildRev(100, Operation.LE);
    BitmapMask mask = factory.contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask, node.apply(0, mask.clone()));
    assertEquals(without(mask.clone(), 0), node.apply(10, mask.clone()));
  }

  @Test
  public void testLessThanRev() {
    IntNode<BitmapMask> node = buildRev(100, Operation.LT);
    BitmapMask mask = factory.contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(without(mask.clone(), 0), node.apply(0, mask.clone()));
    assertEquals(without(without(mask.clone(), 0), 1), node.apply(10, mask.clone()));
  }

  @Test
  public void testBuildNode() {
    IntNode<BitmapMask> node = new IntNode<>(factory, Operation.EQ);
    node.add(0, 0);
    assertEquals(factory.contiguous( 1), node.apply(0, factory.contiguous( 1)));
    node.add(0, 1);
    assertEquals(factory.contiguous( 2), node.apply(0, factory.contiguous( 2)));
  }

  private IntNode<BitmapMask> build(int count, Operation relation) {
    IntNode<BitmapMask> node = new IntNode<>(factory, relation);
    for (int i = 0; i < count; ++i) {
      node.add(i * 10, i);
    }
    return node.optimise();
  }

  private IntNode<BitmapMask> buildRev(int count, Operation relation) {
    IntNode<BitmapMask> node = new IntNode<>(factory, relation);
    for (int i = count - 1; i >= 0; --i) {
      node.add(i * 10, i);
    }
    return node.optimise();
  }
}