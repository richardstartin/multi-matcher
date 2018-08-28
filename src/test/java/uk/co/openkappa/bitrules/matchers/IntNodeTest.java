package uk.co.openkappa.bitrules.matchers;

import org.junit.jupiter.api.Test;
import uk.co.openkappa.bitrules.ContainerMask;
import uk.co.openkappa.bitrules.Operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.openkappa.bitrules.ContainerMask.contiguous;
import static uk.co.openkappa.bitrules.Mask.with;
import static uk.co.openkappa.bitrules.Mask.without;

public class IntNodeTest {

  @Test
  public void testGreaterThan() {
    IntNode<ContainerMask> node = build(100, Operation.GT);
    ContainerMask mask = contiguous(100);
    assertTrue(node.apply(0, mask.clone()).isEmpty());
    assertEquals(with(new ContainerMask(), 0), node.apply(1, mask.clone()));
  }


  @Test
  public void testGreaterThanOrEqual() {
    IntNode<ContainerMask> node = build(100, Operation.GE);
    ContainerMask mask = contiguous(100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(new ContainerMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(with(new ContainerMask(), 1), 0), node.apply(10, mask.clone()));
  }

  @Test
  public void testEqual() {
    IntNode<ContainerMask> node = build(100, Operation.EQ);
    ContainerMask mask = contiguous(100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(new ContainerMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(new ContainerMask(), 1), node.apply(10, mask.clone()));
  }


  @Test
  public void testLessThanOrEqual() {
    IntNode<ContainerMask> node = build(100, Operation.LE);
    ContainerMask mask = contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask, node.apply(0, mask.clone()));
    assertEquals(without(mask.clone(), 0), node.apply(10, mask.clone()));
  }

  @Test
  public void testLessThan() {
    IntNode<ContainerMask> node = build(100, Operation.LT);
    ContainerMask mask = contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(without(mask.clone(), 0), node.apply(0, mask.clone()));
    assertEquals(without(without(mask.clone(), 0), 1), node.apply(10, mask.clone()));
  }

  @Test
  public void testGreaterThanRev() {
    IntNode<ContainerMask> node = buildRev(100, Operation.GT);
    ContainerMask mask = contiguous(100);
    assertTrue(node.apply(0, mask.clone()).isEmpty());
    assertEquals(with(new ContainerMask(), 0), node.apply(1, mask.clone()));
  }


  @Test
  public void testGreaterThanOrEqualRev() {
    IntNode<ContainerMask> node = buildRev(100, Operation.GE);
    ContainerMask mask = contiguous(100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(new ContainerMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(with(new ContainerMask(), 0), 1), node.apply(10, mask.clone()));
  }

  @Test
  public void testEqualRev() {
    IntNode<ContainerMask> node = buildRev(100, Operation.EQ);
    ContainerMask mask = contiguous(100);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(new ContainerMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(new ContainerMask(), 1), node.apply(10, mask.clone()));
  }


  @Test
  public void testLessThanOrEqualRev() {
    IntNode<ContainerMask> node = buildRev(100, Operation.LE);
    ContainerMask mask = contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask, node.apply(0, mask.clone()));
    assertEquals(without(mask.clone(), 0), node.apply(10, mask.clone()));
  }

  @Test
  public void testLessThanRev() {
    IntNode<ContainerMask> node = buildRev(100, Operation.LT);
    ContainerMask mask = contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(without(mask.clone(), 0), node.apply(0, mask.clone()));
    assertEquals(without(without(mask.clone(), 0), 1), node.apply(10, mask.clone()));
  }

  @Test
  public void testBuildNode() {
    IntNode<ContainerMask> node = new IntNode<>(Operation.EQ, new ContainerMask());
    node.add(0, 0);
    assertEquals(contiguous( 1), node.apply(0, contiguous( 1)));
    node.add(0, 1);
    assertEquals(contiguous( 2), node.apply(0, contiguous( 2)));
  }

  private IntNode<ContainerMask> build(int count, Operation relation) {
    IntNode<ContainerMask> node = new IntNode<>(relation, new ContainerMask());
    for (int i = 0; i < count; ++i) {
      node.add(i * 10, i);
    }
    return node.optimise();
  }

  private IntNode<ContainerMask> buildRev(int count, Operation relation) {
    IntNode<ContainerMask> node = new IntNode<>(relation, new ContainerMask());
    for (int i = count - 1; i >= 0; --i) {
      node.add(i * 10, i);
    }
    return node.optimise();
  }
}