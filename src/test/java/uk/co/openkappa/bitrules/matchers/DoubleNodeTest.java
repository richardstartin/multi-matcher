package uk.co.openkappa.bitrules.matchers;

import org.junit.Test;
import uk.co.openkappa.bitrules.ContainerMask;
import uk.co.openkappa.bitrules.Operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.co.openkappa.bitrules.ContainerMask.contiguous;
import static uk.co.openkappa.bitrules.Mask.with;

public class DoubleNodeTest {

  private static final ContainerMask ZERO = with(new ContainerMask(), 0);
  private static final ContainerMask ONE = with(new ContainerMask(), 1);
  private static final ContainerMask ZERO_OR_ONE = ZERO.or(ONE);

  @Test
  public void testGreaterThan() {
    DoubleNode<ContainerMask> node = build(100, Operation.GT);
    ContainerMask mask = contiguous( 100);
    assertTrue(node.match(0, mask.clone()).isEmpty());
    assertEquals(ZERO, node.match(1, mask.clone()));
    assertEquals(ZERO_OR_ONE, node.match(11, mask.clone()));
  }

  @Test
  public void testEqual() {
    DoubleNode<ContainerMask> node = build(100, Operation.EQ);
    ContainerMask mask = contiguous( 100);
    assertTrue(node.match(-1, mask.clone()).isEmpty());
    assertEquals(ZERO, node.match(0, mask.clone()));
    assertEquals(ONE, node.match(10, mask.clone()));
  }

  @Test
  public void testLessThan() {
    DoubleNode<ContainerMask> node = build(100, Operation.LT);
    ContainerMask mask = contiguous( 100);
    assertTrue(node.match(1001, mask.clone()).isEmpty());
    assertEquals(mask.andNot(ZERO), node.match(0, mask.clone()));
    assertEquals(mask.andNot(ZERO_OR_ONE), node.match(10, mask.clone()));
  }

  @Test
  public void testGreaterThanRev() {
    DoubleNode<ContainerMask> node = buildRev(100, Operation.GT);
    ContainerMask mask = contiguous( 100);
    assertTrue(node.match(0, mask.clone()).isEmpty());
    assertEquals(ZERO, node.match(1, mask.clone()));
  }

  @Test
  public void testEqualRev() {
    DoubleNode<ContainerMask> node = buildRev(100, Operation.EQ);
    ContainerMask mask = contiguous( 100);
    assertTrue(node.match(-1, mask.clone()).isEmpty());
    assertEquals(ZERO, node.match(0, mask.clone()));
    assertEquals(ONE, node.match(10, mask.clone()));
  }

  @Test
  public void testLessThanRev() {
    DoubleNode<ContainerMask> node = buildRev(100, Operation.LT);
    ContainerMask mask = contiguous( 100);
    assertTrue(node.match(1001, mask.clone()).isEmpty());
    assertEquals(mask.andNot(ZERO), node.match(0, mask.clone()));
    assertEquals(mask.andNot(ZERO_OR_ONE), node.match(10, mask.clone()));
  }

  @Test
  public void testBuildNode() {
    DoubleNode<ContainerMask> node = new DoubleNode<>(Operation.EQ, new ContainerMask());
    node.add(0, (short)0);
    assertEquals(contiguous(1), node.match(0, contiguous(1)));
    node.add(0, (short)1);
    assertEquals(contiguous(2), node.match(0, contiguous(2)));
  }

  private DoubleNode<ContainerMask> build(int count, Operation relation) {
    DoubleNode<ContainerMask> node = new DoubleNode<>(relation, new ContainerMask());
    for (int i = 0; i < count; ++i) {
      node.add(i * 10, (short) i);
    }
    return node.optimise();
  }

  private DoubleNode<ContainerMask> buildRev(int count, Operation relation) {
    DoubleNode<ContainerMask> node = new DoubleNode<>(relation, new ContainerMask());
    for (int i = count - 1; i >= 0; --i) {
      node.add(i * 10, (short) i);
    }
    return node.optimise();
  }
}