package uk.co.openkappa.bitrules.matchers;

import org.junit.Test;
import uk.co.openkappa.bitrules.ContainerMask;
import uk.co.openkappa.bitrules.Operation;

import java.time.LocalDate;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.co.openkappa.bitrules.ContainerMask.contiguous;
import static uk.co.openkappa.bitrules.Mask.with;
import static uk.co.openkappa.bitrules.matchers.Masks.singleton;

public class ComparableNodeTest {

  private static final ContainerMask ZERO = with(new ContainerMask(), 0);
  private static final ContainerMask ONE = with(new ContainerMask(), 1);
  private static final ContainerMask ZERO_OR_ONE = ZERO.or(ONE);

  @Test
  public void testGreaterThan() {
    ComparableNode<LocalDate, ContainerMask> node = build(100, Operation.GT);
    ContainerMask mask = contiguous(100);
    assertTrue(node.match(LocalDate.ofEpochDay(0), mask.clone()).isEmpty());
    assertEquals(ZERO, node.match(LocalDate.ofEpochDay(1), mask.clone()));
    assertEquals(ZERO_OR_ONE, node.match(LocalDate.ofEpochDay(11), mask.clone()));
  }

  @Test
  public void testEqual() {
    ComparableNode<LocalDate, ContainerMask> node = build(100, Operation.EQ);
    ContainerMask mask = contiguous(100);
    assertTrue(node.match(LocalDate.ofEpochDay((1)), mask.clone()).isEmpty());
    assertEquals(ZERO, node.match(LocalDate.ofEpochDay(0), mask.clone()));
    assertEquals(ONE, node.match(LocalDate.ofEpochDay(10), mask.clone()));
  }

  @Test
  public void testLessThan() {
    ComparableNode<LocalDate, ContainerMask> node = build(100, Operation.LT);
    ContainerMask mask = contiguous(100);
    assertTrue(node.match(LocalDate.ofEpochDay(1001), mask.clone()).isEmpty());
    assertEquals(mask.andNot(ZERO), node.match(LocalDate.ofEpochDay(0), mask.clone()));
    assertEquals(mask.andNot(ZERO_OR_ONE), node.match(LocalDate.ofEpochDay(10), mask.clone()));
  }

  @Test
  public void testGreaterThanRev() {
    ComparableNode<LocalDate, ContainerMask> node = buildRev(100, Operation.GT);
    ContainerMask mask = contiguous(100);
    assertTrue(node.match(LocalDate.ofEpochDay(0), mask.clone()).isEmpty());
    assertEquals(ZERO, node.match(LocalDate.ofEpochDay(1), mask.clone()));
  }

  @Test
  public void testEqualRev() {
    ComparableNode<LocalDate, ContainerMask> node = buildRev(100, Operation.EQ);
    ContainerMask mask = contiguous(100);
    assertTrue(node.match(LocalDate.ofEpochDay(1), mask.clone()).isEmpty());
    assertEquals(ZERO, node.match(LocalDate.ofEpochDay(0), mask.clone()));
    assertEquals(ONE, node.match(LocalDate.ofEpochDay(10), mask.clone()));
  }

  @Test
  public void testLessThanRev() {
    ComparableNode<LocalDate, ContainerMask> node = buildRev(100, Operation.LT);
    ContainerMask mask = contiguous(100);
    assertTrue(node.match(LocalDate.ofEpochDay(1001), mask.clone()).isEmpty());
    assertEquals(mask.andNot(ZERO), node.match(LocalDate.ofEpochDay(0), mask.clone()));
    assertEquals(mask.andNot(ZERO_OR_ONE), node.match(LocalDate.ofEpochDay(10), mask.clone()));
  }

  @Test
  public void testBuildNode() {
    DoubleNode<ContainerMask> node = new DoubleNode<>(Operation.EQ, new ContainerMask());
    node.add(0, 0);
    assertEquals(contiguous(1), node.match(0, contiguous(1)));
    node.add(0, 1);
    assertEquals(contiguous(2), node.match(0, contiguous(2)));
  }

  private ComparableNode<LocalDate, ContainerMask> build(int count, Operation operation) {
    ComparableNode<LocalDate, ContainerMask> node = new ComparableNode<>(Comparator.<LocalDate>naturalOrder(), operation, singleton(ContainerMask.class));
    for (int i = 0; i < count; ++i) {
      node.add(LocalDate.ofEpochDay(i * 10),  i);
    }
    return node.optimise();
  }

  private ComparableNode<LocalDate, ContainerMask> buildRev(int count, Operation operation) {
    ComparableNode<LocalDate, ContainerMask> node = new ComparableNode<>(Comparator.<LocalDate>naturalOrder(), operation, singleton(ContainerMask.class));
    for (int i = count - 1; i >= 0; --i) {
      node.add(LocalDate.ofEpochDay(i * 10),  i);
    }
    return node.optimise();
  }
}