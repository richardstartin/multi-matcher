package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.Operation;
import io.github.richardstartin.multimatcher.core.masks.BitsetMask;
import io.github.richardstartin.multimatcher.core.masks.MaskFactory;
import io.github.richardstartin.multimatcher.core.matchers.nodes.LongNode;
import org.junit.jupiter.api.Test;

import static io.github.richardstartin.multimatcher.core.Mask.with;
import static io.github.richardstartin.multimatcher.core.Mask.without;
import static io.github.richardstartin.multimatcher.core.masks.BitsetMask.factory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LongMutableNodeTest {

  private static MaskFactory<BitsetMask> FACTORY = factory(200);

  @Test
  public void testGreaterThan() {
    LongNode<BitsetMask> node = build(100, Operation.GT);
    BitsetMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(0L, mask.clone()).isEmpty());
    assertEquals(with(FACTORY.newMask(), 0), node.apply(1L, mask.clone()));
  }


  @Test
  public void testGreaterThanOrEqual() {
    LongNode<BitsetMask> node = build(100, Operation.GE);
    BitsetMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(-1L, mask.clone()).isEmpty());
    assertEquals(with(FACTORY.newMask(), 0), node.apply(0L, mask.clone()));
    assertEquals(with(with(FACTORY.newMask(), 0), 1), node.apply(10L, mask.clone()));
  }

  @Test
  public void testEqual() {
    LongNode<BitsetMask> node = build(100, Operation.EQ);
    BitsetMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(-1L, mask.clone()).isEmpty());
    assertEquals(with(FACTORY.newMask(), 0), node.apply(0L, mask.clone()));
    assertEquals(with(FACTORY.newMask(), 1), node.apply(10L, mask.clone()));
  }


  @Test
  public void testLessThanOrEqual() {
    LongNode<BitsetMask> node = build(100, Operation.LE);
    BitsetMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask, node.apply(0L, mask.clone()));
    assertEquals(without(mask.clone(), 0), node.apply(10L, mask.clone()));
  }

  @Test
  public void testLessThan() {
    LongNode<BitsetMask> node = build(100, Operation.LT);
    BitsetMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(without(mask.clone(), 0), node.apply(0L, mask.clone()));
    assertEquals(without(without(mask.clone(), 0), 1), node.apply(10L, mask.clone()));
  }

  @Test
  public void testGreaterThanRev() {
    LongNode<BitsetMask> node = buildRev(100, Operation.GT);
    BitsetMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(0L, mask.clone()).isEmpty());
    assertEquals(with(FACTORY.newMask(), 0), node.apply(1L, mask.clone()));
  }

  @Test
  public void testBuildNode() {
    LongNode<BitsetMask> node = new LongNode<>(FACTORY, Operation.EQ);
    node.add(0, 0);
    assertEquals(FACTORY.contiguous(1), node.apply(0, FACTORY.contiguous(1)));
    node.add(0, 1);
    assertEquals(FACTORY.contiguous(2), node.apply(0, FACTORY.contiguous(2)));
  }

  @Test
  public void testGreaterThanOrEqualRev() {
    LongNode<BitsetMask> node = buildRev(100, Operation.GE);
    BitsetMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(-1L, mask.clone()).isEmpty());
    assertEquals(with(FACTORY.newMask(), 0), node.apply(0L, mask.clone()));
    assertEquals(with(with(FACTORY.newMask(), 0), 1), node.apply(10L, mask.clone()));
  }

  @Test
  public void testEqualRev() {
    LongNode<BitsetMask> node = buildRev(100, Operation.EQ);
    BitsetMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(-1L, mask.clone()).isEmpty());
    assertEquals(with(FACTORY.newMask(), 0), node.apply(0L, mask.clone()));
    assertEquals(with(FACTORY.newMask(), 1), node.apply(10L, mask.clone()));
  }


  @Test
  public void testLessThanOrEqualRev() {
    LongNode<BitsetMask> node = buildRev(100, Operation.LE);
    BitsetMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask, node.apply(0L, mask.clone()));
    assertEquals(without(mask, 0), node.apply(10L, mask.clone()));
  }

  @Test
  public void testLessThanRev() {
    LongNode<BitsetMask> node = buildRev(100, Operation.LT);
    BitsetMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(without(mask.clone(), 0), node.apply(0L, mask.clone()));
    assertEquals(without(without(mask.clone(), 0), 1), node.apply(10L, mask.clone()));
  }


  private LongNode<BitsetMask> build(int count, Operation relation) {
    LongNode<BitsetMask> node = new LongNode<>(FACTORY, relation);
    for (int i = 0; i < count; ++i) {
      node.add(i * 10, i);
    }
    return node.optimise();
  }

  private LongNode<BitsetMask> buildRev(int count, Operation relation) {
    LongNode<BitsetMask> node = new LongNode<>(FACTORY, relation);
    for (int i = count - 1; i >= 0; --i) {
      node.add(i * 10, i);
    }
    return node.optimise();
  }

}