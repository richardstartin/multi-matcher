package uk.co.openkappa.bitrules.matchers;

import org.junit.jupiter.api.Test;
import uk.co.openkappa.bitrules.WordMask;
import uk.co.openkappa.bitrules.Operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.openkappa.bitrules.WordMask.contiguous;
import static uk.co.openkappa.bitrules.Mask.with;
import static uk.co.openkappa.bitrules.Mask.without;

public class IntNodeTestTiny {
  @Test
  public void testGreaterThan() {
    IntNode<WordMask> node = build(5, Operation.GT);
    WordMask mask = contiguous(5);
    assertTrue(node.apply(0, mask.clone()).isEmpty());
    assertEquals(with(new WordMask(), 0), node.apply(1, mask.clone()));
  }


  @Test
  public void testGreaterThanOrEqual() {
    IntNode<WordMask> node = build(5, Operation.GE);
    WordMask mask = contiguous(5);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(new WordMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(with(new WordMask(), 1), 0), node.apply(10, mask.clone()));
  }

  @Test
  public void testEqual() {
    IntNode<WordMask> node = build(5, Operation.EQ);
    WordMask mask = contiguous(5);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(new WordMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(new WordMask(), 1), node.apply(10, mask.clone()));
  }


  @Test
  public void testLessThanOrEqual() {
    IntNode<WordMask> node = build(5, Operation.LE);
    WordMask mask = contiguous(5);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask, node.apply(0, mask.clone()));
    assertEquals(without(mask.clone(), 0), node.apply(10, mask.clone()));
  }

  @Test
  public void testLessThan() {
    IntNode<WordMask> node = build(5, Operation.LT);
    WordMask mask = contiguous(5);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(without(mask.clone(), 0), node.apply(0, mask.clone()));
    assertEquals(without(without(mask.clone(), 0), 1), node.apply(10, mask.clone()));
  }

  @Test
  public void testGreaterThanRev() {
    IntNode<WordMask> node = buildRev(5, Operation.GT);
    WordMask mask = contiguous(5);
    assertTrue(node.apply(0, mask.clone()).isEmpty());
    assertEquals(with(new WordMask(), 0), node.apply(1, mask.clone()));
  }


  @Test
  public void testGreaterThanOrEqualRev() {
    IntNode<WordMask> node = buildRev(5, Operation.GE);
    WordMask mask = contiguous(5);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(new WordMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(with(new WordMask(), 0), 1), node.apply(10, mask.clone()));
  }

  @Test
  public void testEqualRev() {
    IntNode<WordMask> node = buildRev(5, Operation.EQ);
    WordMask mask = contiguous(5);
    assertTrue(node.apply(-1, mask.clone()).isEmpty());
    assertEquals(with(new WordMask(), 0), node.apply(0, mask.clone()));
    assertEquals(with(new WordMask(), 1), node.apply(10, mask.clone()));
  }


  @Test
  public void testLessThanOrEqualRev() {
    IntNode<WordMask> node = buildRev(5, Operation.LE);
    WordMask mask = contiguous(5);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(mask, node.apply(0, mask.clone()));
    assertEquals(without(mask.clone(), 0), node.apply(10, mask.clone()));
  }

  @Test
  public void testLessThanRev() {
    IntNode<WordMask> node = buildRev(5, Operation.LT);
    WordMask mask = contiguous(5);
    assertTrue(node.apply(1001, mask.clone()).isEmpty());
    assertEquals(without(mask.clone(), 0), node.apply(0, mask.clone()));
    assertEquals(without(without(mask.clone(), 0), 1), node.apply(10, mask.clone()));
  }

  @Test
  public void testBuildNode() {
    IntNode<WordMask> node = new IntNode<>(Operation.EQ, new WordMask());
    node.add(0, 0);
    assertEquals(contiguous( 1), node.apply(0, contiguous( 1)));
    node.add(0, 1);
    assertEquals(contiguous( 2), node.apply(0, contiguous( 2)));
  }

  private IntNode<WordMask> build(int count, Operation relation) {
    IntNode<WordMask> node = new IntNode<>(relation, new WordMask());
    for (int i = 0; i < count; ++i) {
      node.add(i * 10, i);
    }
    return node.optimise();
  }

  private IntNode<WordMask> buildRev(int count, Operation relation) {
    IntNode<WordMask> node = new IntNode<>(relation, new WordMask());
    for (int i = count - 1; i >= 0; --i) {
      node.add(i * 10, i);
    }
    return node.optimise();
  }
}
