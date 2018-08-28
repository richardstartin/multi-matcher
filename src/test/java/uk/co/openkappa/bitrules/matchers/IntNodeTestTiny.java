package uk.co.openkappa.bitrules.matchers;

import org.junit.jupiter.api.Test;
import uk.co.openkappa.bitrules.masks.TinyMask;
import uk.co.openkappa.bitrules.Operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.openkappa.bitrules.masks.TinyMask.contiguous;
import static uk.co.openkappa.bitrules.Mask.with;
import static uk.co.openkappa.bitrules.Mask.without;

public class IntNodeTestTiny {
  @Test
  public void testGreaterThan() {
    IntMatcher.IntNode<TinyMask> node = build(5, Operation.GT);
    TinyMask mask = contiguous(5);
    assertTrue(node.apply(0, mask.clone()).isEmpty());
    assertEquals(with(new TinyMask(), 0), node.apply(1, mask.clone()));
  }


  @Test
  public void testGreaterThanOrEqual() {
    IntMatcher.IntNode<TinyMask> node = build(5, Operation.GE);
    TinyMask mask = contiguous(5);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(new TinyMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(with(new TinyMask(), 1), 0), node.apply(10, mask.clone()));
  }

  @Test
  public void testEqual() {
    IntMatcher.IntNode<TinyMask> node = build(5, Operation.EQ);
    TinyMask mask = contiguous(5);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(new TinyMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(new TinyMask(), 1), node.apply(10, mask.clone()));
  }


  @Test
  public void testLessThanOrEqual() {
    IntMatcher.IntNode<TinyMask> node = build(5, Operation.LE);
    TinyMask mask = contiguous(5);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask, node.apply(0, mask.clone()));
    assertEquals(without(mask.clone(), 0), node.apply(10, mask.clone()));
  }

  @Test
  public void testLessThan() {
    IntMatcher.IntNode<TinyMask> node = build(5, Operation.LT);
    TinyMask mask = contiguous(5);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(without(mask.clone(), 0), node.apply(0, mask.clone()));
    assertEquals(without(without(mask.clone(), 0), 1), node.apply(10, mask.clone()));
  }

  @Test
  public void testGreaterThanRev() {
    IntMatcher.IntNode<TinyMask> node = buildRev(5, Operation.GT);
    TinyMask mask = contiguous(5);
    assertTrue(node.apply(0, mask.clone()).isEmpty());
    assertEquals(with(new TinyMask(), 0), node.apply(1, mask.clone()));
  }


  @Test
  public void testGreaterThanOrEqualRev() {
    IntMatcher.IntNode<TinyMask> node = buildRev(5, Operation.GE);
    TinyMask mask = contiguous(5);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(new TinyMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(with(new TinyMask(), 0), 1), node.apply(10, mask.clone()));
  }

  @Test
  public void testEqualRev() {
    IntMatcher.IntNode<TinyMask> node = buildRev(5, Operation.EQ);
    TinyMask mask = contiguous(5);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(new TinyMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(new TinyMask(), 1), node.apply(10, mask.clone()));
  }


  @Test
  public void testLessThanOrEqualRev() {
    IntMatcher.IntNode<TinyMask> node = buildRev(5, Operation.LE);
    TinyMask mask = contiguous(5);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask, node.apply(0, mask.clone()));
    assertEquals(without(mask.clone(), 0), node.apply(10, mask.clone()));
  }

  @Test
  public void testLessThanRev() {
    IntMatcher.IntNode<TinyMask> node = buildRev(5, Operation.LT);
    TinyMask mask = contiguous(5);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(without(mask.clone(), 0), node.apply(0, mask.clone()));
    assertEquals(without(without(mask.clone(), 0), 1), node.apply(10, mask.clone()));
  }

  @Test
  public void testBuildNode() {
    IntMatcher.IntNode<TinyMask> node = new IntMatcher.IntNode<>(Operation.EQ, new TinyMask());
    node.add(0, 0);
    assertEquals(contiguous( 1), node.apply(0, contiguous( 1)));
    node.add(0, 1);
    assertEquals(contiguous( 2), node.apply(0, contiguous( 2)));
  }

  private IntMatcher.IntNode<TinyMask> build(int count, Operation relation) {
    IntMatcher.IntNode<TinyMask> node = new IntMatcher.IntNode<>(relation, new TinyMask());
    for (int i = 0; i < count; ++i) {
      node.add(i * 10, i);
    }
    return node.optimise();
  }

  private IntMatcher.IntNode<TinyMask> buildRev(int count, Operation relation) {
    IntMatcher.IntNode<TinyMask> node = new IntMatcher.IntNode<>(relation, new TinyMask());
    for (int i = count - 1; i >= 0; --i) {
      node.add(i * 10, i);
    }
    return node.optimise();
  }
}
