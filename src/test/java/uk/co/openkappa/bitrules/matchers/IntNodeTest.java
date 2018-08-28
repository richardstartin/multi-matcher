package uk.co.openkappa.bitrules.matchers;

import org.junit.jupiter.api.Test;
import uk.co.openkappa.bitrules.masks.SmallMask;
import uk.co.openkappa.bitrules.Operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.openkappa.bitrules.masks.SmallMask.contiguous;
import static uk.co.openkappa.bitrules.Mask.with;
import static uk.co.openkappa.bitrules.Mask.without;

public class IntNodeTest {

  @Test
  public void testGreaterThan() {
    IntMatcher.IntNode<SmallMask> node = build(100, Operation.GT);
    SmallMask mask = contiguous(100);
    assertTrue(node.apply(0, mask.clone()).isEmpty());
    assertEquals(with(new SmallMask(), 0), node.apply(1, mask.clone()));
  }


  @Test
  public void testGreaterThanOrEqual() {
    IntMatcher.IntNode<SmallMask> node = build(100, Operation.GE);
    SmallMask mask = contiguous(100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(new SmallMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(with(new SmallMask(), 1), 0), node.apply(10, mask.clone()));
  }

  @Test
  public void testEqual() {
    IntMatcher.IntNode<SmallMask> node = build(100, Operation.EQ);
    SmallMask mask = contiguous(100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(new SmallMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(new SmallMask(), 1), node.apply(10, mask.clone()));
  }


  @Test
  public void testLessThanOrEqual() {
    IntMatcher.IntNode<SmallMask> node = build(100, Operation.LE);
    SmallMask mask = contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask, node.apply(0, mask.clone()));
    assertEquals(without(mask.clone(), 0), node.apply(10, mask.clone()));
  }

  @Test
  public void testLessThan() {
    IntMatcher.IntNode<SmallMask> node = build(100, Operation.LT);
    SmallMask mask = contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(without(mask.clone(), 0), node.apply(0, mask.clone()));
    assertEquals(without(without(mask.clone(), 0), 1), node.apply(10, mask.clone()));
  }

  @Test
  public void testGreaterThanRev() {
    IntMatcher.IntNode<SmallMask> node = buildRev(100, Operation.GT);
    SmallMask mask = contiguous(100);
    assertTrue(node.apply(0, mask.clone()).isEmpty());
    assertEquals(with(new SmallMask(), 0), node.apply(1, mask.clone()));
  }


  @Test
  public void testGreaterThanOrEqualRev() {
    IntMatcher.IntNode<SmallMask> node = buildRev(100, Operation.GE);
    SmallMask mask = contiguous(100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(new SmallMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(with(new SmallMask(), 0), 1), node.apply(10, mask.clone()));
  }

  @Test
  public void testEqualRev() {
    IntMatcher.IntNode<SmallMask> node = buildRev(100, Operation.EQ);
    SmallMask mask = contiguous(100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(new SmallMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(new SmallMask(), 1), node.apply(10, mask.clone()));
  }


  @Test
  public void testLessThanOrEqualRev() {
    IntMatcher.IntNode<SmallMask> node = buildRev(100, Operation.LE);
    SmallMask mask = contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask, node.apply(0, mask.clone()));
    assertEquals(without(mask.clone(), 0), node.apply(10, mask.clone()));
  }

  @Test
  public void testLessThanRev() {
    IntMatcher.IntNode<SmallMask> node = buildRev(100, Operation.LT);
    SmallMask mask = contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(without(mask.clone(), 0), node.apply(0, mask.clone()));
    assertEquals(without(without(mask.clone(), 0), 1), node.apply(10, mask.clone()));
  }

  @Test
  public void testBuildNode() {
    IntMatcher.IntNode<SmallMask> node = new IntMatcher.IntNode<>(Operation.EQ, new SmallMask());
    node.add(0, 0);
    assertEquals(contiguous( 1), node.apply(0, contiguous( 1)));
    node.add(0, 1);
    assertEquals(contiguous( 2), node.apply(0, contiguous( 2)));
  }

  private IntMatcher.IntNode<SmallMask> build(int count, Operation relation) {
    IntMatcher.IntNode<SmallMask> node = new IntMatcher.IntNode<>(relation, new SmallMask());
    for (int i = 0; i < count; ++i) {
      node.add(i * 10, i);
    }
    return node.optimise();
  }

  private IntMatcher.IntNode<SmallMask> buildRev(int count, Operation relation) {
    IntMatcher.IntNode<SmallMask> node = new IntMatcher.IntNode<>(relation, new SmallMask());
    for (int i = count - 1; i >= 0; --i) {
      node.add(i * 10, i);
    }
    return node.optimise();
  }
}