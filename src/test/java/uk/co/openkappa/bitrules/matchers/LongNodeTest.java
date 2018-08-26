package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.ContainerMask;
import uk.co.openkappa.bitrules.Operation;
import org.junit.Test;
import org.roaringbitmap.ArrayContainer;

import static org.junit.Assert.*;
import static uk.co.openkappa.bitrules.ContainerMask.contiguous;
import static uk.co.openkappa.bitrules.Mask.with;
import static uk.co.openkappa.bitrules.Mask.without;

public class LongNodeTest {


  @Test
  public void testGreaterThan() {
    LongNode<ContainerMask> node = build(100, Operation.GT);
    ContainerMask mask = contiguous(100);
    assertTrue(node.apply(0L, mask.clone()).isEmpty());
    assertEquals(with(new ContainerMask(), 0), node.apply(1L, mask.clone()));
  }


  @Test
  public void testGreaterThanOrEqual() {
    LongNode<ContainerMask> node = build(100, Operation.GE);
    ContainerMask mask = contiguous(100);
    assertTrue(node.apply(-1L, mask.clone()).isEmpty());
    assertEquals(with(new ContainerMask(), 0), node.apply(0L, mask.clone()));
    assertEquals(with(with(new ContainerMask(), 0), 1), node.apply(10L, mask.clone()));
  }

  @Test
  public void testEqual() {
    LongNode<ContainerMask> node = build(100, Operation.EQ);
    ContainerMask mask = contiguous(100);
    assertTrue(node.apply(-1L, mask.clone()).isEmpty());
    assertEquals(with(new ContainerMask(), 0), node.apply(0L, mask.clone()));
    assertEquals(with(new ContainerMask(), 1), node.apply(10L, mask.clone()));
  }


  @Test
  public void testLessThanOrEqual() {
    LongNode<ContainerMask> node = build(100, Operation.LE);
    ContainerMask mask = contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask, node.apply(0L, mask.clone()));
    assertEquals(without(mask.clone(), 0), node.apply(10L, mask.clone()));
  }

  @Test
  public void testLessThan() {
    LongNode<ContainerMask> node = build(100, Operation.LT);
    ContainerMask mask = contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(without(mask.clone(), 0), node.apply(0L, mask.clone()));
    assertEquals(without(without(mask.clone(), 0), 1), node.apply(10L, mask.clone()));
  }

  @Test
  public void testGreaterThanRev() {
    LongNode<ContainerMask> node = buildRev(100, Operation.GT);
    ContainerMask mask = contiguous(100);
    assertTrue(node.apply(0L, mask.clone()).isEmpty());
    assertEquals(with(new ContainerMask(), 0), node.apply(1L, mask.clone()));
  }

  @Test
  public void testBuildNode() {
    LongNode<ContainerMask> node = new LongNode<>(Operation.EQ, new ContainerMask());
    node.add(0, (short)0);
    assertEquals(contiguous(1), node.apply(0, contiguous(1)));
    node.add(0, (short)1);
    assertEquals(contiguous(2), node.apply(0, contiguous(2)));
  }

  @Test
  public void testGreaterThanOrEqualRev() {
    LongNode<ContainerMask> node = buildRev(100, Operation.GE);
    ContainerMask mask = contiguous(100);
    assertTrue(node.apply(-1L, mask.clone()).isEmpty());
    assertEquals(with(new ContainerMask(), 0), node.apply(0L, mask.clone()));
    assertEquals(with(with(new ContainerMask(), 0), 1), node.apply(10L, mask.clone()));
  }

  @Test
  public void testEqualRev() {
    LongNode<ContainerMask> node = buildRev(100, Operation.EQ);
    ContainerMask mask = contiguous(100);
    assertTrue(node.apply(-1L, mask.clone()).isEmpty());
    assertEquals(with(new ContainerMask(), 0), node.apply(0L, mask.clone()));
    assertEquals(with(new ContainerMask(), 1), node.apply(10L, mask.clone()));
  }


  @Test
  public void testLessThanOrEqualRev() {
    LongNode<ContainerMask> node = buildRev(100, Operation.LE);
    ContainerMask mask = contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask, node.apply(0L, mask.clone()));
    assertEquals(without(mask, 0), node.apply(10L, mask.clone()));
  }

  @Test
  public void testLessThanRev() {
    LongNode<ContainerMask> node = buildRev(100, Operation.LT);
    ContainerMask mask = contiguous(100);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(without(mask.clone(), 0), node.apply(0L, mask.clone()));
    assertEquals(without(without(mask.clone(), 0), 1), node.apply(10L, mask.clone()));
  }


  private LongNode<ContainerMask> build(int count, Operation relation) {
    LongNode<ContainerMask> node = new LongNode<>(relation, new ContainerMask());
    for (int i = 0; i < count; ++i) {
      node.add(i * 10, (short)i);
    }
    return node.optimise();
  }

  private LongNode<ContainerMask> buildRev(int count, Operation relation) {
    LongNode<ContainerMask> node = new LongNode<>(relation, new ContainerMask());
    for (int i = count - 1; i >= 0; --i) {
      node.add(i * 10, (short)i);
    }
    return node.optimise();
  }

}