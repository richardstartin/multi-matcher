package uk.co.openkappa.bitrules.matchers;


import org.junit.jupiter.api.Test;
import uk.co.openkappa.bitrules.masks.SmallMask;
import uk.co.openkappa.bitrules.Operation;
import uk.co.openkappa.bitrules.matchers.nodes.DoubleNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.openkappa.bitrules.masks.SmallMask.FACTORY;
import static uk.co.openkappa.bitrules.Mask.with;

public class DoubleMutableNodeTest {

  private static final SmallMask ZERO = with(new SmallMask(), 0);
  private static final SmallMask ONE = with(new SmallMask(), 1);
  private static final SmallMask ZERO_OR_ONE = ZERO.or(ONE);

  @Test
  public void testGreaterThan() {
    DoubleNode<SmallMask> node = build(100, Operation.GT);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.match(0, mask.clone()).isEmpty());
    assertEquals(ZERO, node.match(1, mask.clone()));
    assertEquals(ZERO_OR_ONE, node.match(11, mask.clone()));
  }

  @Test
  public void testEqual() {
    DoubleNode<SmallMask> node = build(100, Operation.EQ);
    SmallMask mask = FACTORY.contiguous( 100);
    assertTrue(node.match(-1, mask.clone()).isEmpty());
    assertEquals(ZERO, node.match(0, mask.clone()));
    assertEquals(ONE, node.match(10, mask.clone()));
  }

  @Test
  public void testLessThan() {
    DoubleNode<SmallMask> node = build(100, Operation.LT);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.match(1001, mask.clone()).isEmpty());
    assertEquals(mask.andNot(ZERO), node.match(0, mask.clone()));
    assertEquals(mask.andNot(ZERO_OR_ONE), node.match(10, mask.clone()));
  }

  @Test
  public void testGreaterThanRev() {
    DoubleNode<SmallMask> node = buildRev(100, Operation.GT);
    SmallMask mask = FACTORY.contiguous( 100);
    assertTrue(node.match(0, mask.clone()).isEmpty());
    assertEquals(ZERO, node.match(1, mask.clone()));
  }

  @Test
  public void testEqualRev() {
    DoubleNode<SmallMask> node = buildRev(100, Operation.EQ);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.match(-1, mask.clone()).isEmpty());
    assertEquals(ZERO, node.match(0, mask.clone()));
    assertEquals(ONE, node.match(10, mask.clone()));
  }

  @Test
  public void testLessThanRev() {
    DoubleNode<SmallMask> node = buildRev(100, Operation.LT);
    SmallMask mask = FACTORY.contiguous(100);
    assertTrue(node.match(1001, mask.clone()).isEmpty());
    assertEquals(mask.andNot(ZERO), node.match(0, mask.clone()));
    assertEquals(mask.andNot(ZERO_OR_ONE), node.match(10, mask.clone()));
  }

  @Test
  public void testBuildNode() {
    DoubleNode<SmallMask> node = new DoubleNode<>(Operation.EQ, new SmallMask());
    node.add(0, 0);
    assertEquals(FACTORY.contiguous(1), node.match(0, FACTORY.contiguous(1)));
    node.add(0, 1);
    assertEquals(FACTORY.contiguous(2), node.match(0, FACTORY.contiguous(2)));
  }

  private DoubleNode<SmallMask> build(int count, Operation relation) {
    DoubleNode<SmallMask> node = new DoubleNode<>(relation, new SmallMask());
    for (int i = 0; i < count; ++i) {
      node.add(i * 10,  i);
    }
    return node.optimise();
  }

  private DoubleNode<SmallMask> buildRev(int count, Operation relation) {
    DoubleNode<SmallMask> node = new DoubleNode<>(relation, new SmallMask());
    for (int i = count - 1; i >= 0; --i) {
      node.add(i * 10,  i);
    }
    return node.optimise();
  }
}