package uk.co.openkappa.bitrules.matchers;

import org.junit.jupiter.api.Test;
import uk.co.openkappa.bitrules.masks.SmallMask;
import uk.co.openkappa.bitrules.Operation;
import uk.co.openkappa.bitrules.matchers.nodes.LongNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.openkappa.bitrules.masks.SmallMask.FACTORY;
import static uk.co.openkappa.bitrules.Mask.with;
import static uk.co.openkappa.bitrules.Mask.without;

public class LongMutableNodeTest {


  @Test
  public void testGreaterThan() {
    LongNode<SmallMask> node = build(100, Operation.GT);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(0L, mask.clone()).isEmpty());
    assertEquals(with(new SmallMask(), 0), node.apply(1L, mask.clone()));
  }


  @Test
  public void testGreaterThanOrEqual() {
    LongNode<SmallMask> node = build(100, Operation.GE);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(-1L, mask.clone()).isEmpty());
    assertEquals(with(new SmallMask(), 0), node.apply(0L, mask.clone()));
    assertEquals(with(with(new SmallMask(), 0), 1), node.apply(10L, mask.clone()));
  }

  @Test
  public void testEqual() {
    LongNode<SmallMask> node = build(100, Operation.EQ);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(-1L, mask.clone()).isEmpty());
    assertEquals(with(new SmallMask(), 0), node.apply(0L, mask.clone()));
    assertEquals(with(new SmallMask(), 1), node.apply(10L, mask.clone()));
  }


  @Test
  public void testLessThanOrEqual() {
    LongNode<SmallMask> node = build(100, Operation.LE);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask, node.apply(0L, mask.clone()));
    assertEquals(without(mask.clone(), 0), node.apply(10L, mask.clone()));
  }

  @Test
  public void testLessThan() {
    LongNode<SmallMask> node = build(100, Operation.LT);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(without(mask.clone(), 0), node.apply(0L, mask.clone()));
    assertEquals(without(without(mask.clone(), 0), 1), node.apply(10L, mask.clone()));
  }

  @Test
  public void testGreaterThanRev() {
    LongNode<SmallMask> node = buildRev(100, Operation.GT);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(0L, mask.clone()).isEmpty());
    assertEquals(with(new SmallMask(), 0), node.apply(1L, mask.clone()));
  }

  @Test
  public void testBuildNode() {
    LongNode<SmallMask> node = new LongNode<>(Operation.EQ, new SmallMask());
    node.add(0, 0);
    assertEquals(FACTORY.contiguous(1), node.apply(0, FACTORY.contiguous(1)));
    node.add(0, 1);
    assertEquals(FACTORY.contiguous(2), node.apply(0, FACTORY.contiguous(2)));
  }

  @Test
  public void testGreaterThanOrEqualRev() {
    LongNode<SmallMask> node = buildRev(100, Operation.GE);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(-1L, mask.clone()).isEmpty());
    assertEquals(with(new SmallMask(), 0), node.apply(0L, mask.clone()));
    assertEquals(with(with(new SmallMask(), 0), 1), node.apply(10L, mask.clone()));
  }

  @Test
  public void testEqualRev() {
    LongNode<SmallMask> node = buildRev(100, Operation.EQ);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(-1L, mask.clone()).isEmpty());
    assertEquals(with(new SmallMask(), 0), node.apply(0L, mask.clone()));
    assertEquals(with(new SmallMask(), 1), node.apply(10L, mask.clone()));
  }


  @Test
  public void testLessThanOrEqualRev() {
    LongNode<SmallMask> node = buildRev(100, Operation.LE);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask, node.apply(0L, mask.clone()));
    assertEquals(without(mask, 0), node.apply(10L, mask.clone()));
  }

  @Test
  public void testLessThanRev() {
    LongNode<SmallMask> node = buildRev(100, Operation.LT);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(without(mask.clone(), 0), node.apply(0L, mask.clone()));
    assertEquals(without(without(mask.clone(), 0), 1), node.apply(10L, mask.clone()));
  }


  private LongNode<SmallMask> build(int count, Operation relation) {
    LongNode<SmallMask> node = new LongNode<>(relation, new SmallMask());
    for (int i = 0; i < count; ++i) {
      node.add(i * 10, i);
    }
    return node.optimise();
  }

  private LongNode<SmallMask> buildRev(int count, Operation relation) {
    LongNode<SmallMask> node = new LongNode<>(relation, new SmallMask());
    for (int i = count - 1; i >= 0; --i) {
      node.add(i * 10, i);
    }
    return node.optimise();
  }

}