package io.github.richardstartin.multimatcher.core.matchers;


import io.github.richardstartin.multimatcher.core.Operation;
import io.github.richardstartin.multimatcher.core.masks.BitsetMask;
import io.github.richardstartin.multimatcher.core.masks.MaskFactory;
import io.github.richardstartin.multimatcher.core.matchers.nodes.DoubleNode;
import org.junit.jupiter.api.Test;

import static io.github.richardstartin.multimatcher.core.Mask.with;
import static io.github.richardstartin.multimatcher.core.masks.BitsetMask.factory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DoubleMutableNodeTest {

    private static final MaskFactory<BitsetMask> FACTORY = factory(200);
    private static final BitsetMask ZERO = with(FACTORY.newMask(), 0);
    private static final BitsetMask ONE = with(FACTORY.newMask(), 1);
    private static final BitsetMask ZERO_OR_ONE = ZERO.or(ONE);

    @Test
    public void testGreaterThan() {
        DoubleNode<BitsetMask> node = build(100, Operation.GT);
        BitsetMask mask = FACTORY.contiguous(100);
        assertTrue(node.match(0, mask.clone()).isEmpty());
        assertEquals(ZERO, node.match(1, mask.clone()));
        assertEquals(ZERO_OR_ONE, node.match(11, mask.clone()));
    }

    @Test
    public void testEqual() {
        DoubleNode<BitsetMask> node = build(100, Operation.EQ);
        BitsetMask mask = FACTORY.contiguous(100);
        assertTrue(node.match(-1, mask.clone()).isEmpty());
        assertEquals(ZERO, node.match(0, mask.clone()));
        assertEquals(ONE, node.match(10, mask.clone()));
    }

    @Test
    public void testLessThan() {
        DoubleNode<BitsetMask> node = build(100, Operation.LT);
        BitsetMask mask = FACTORY.contiguous(100);
        assertTrue(node.match(1001, mask.clone()).isEmpty());
        assertEquals(mask.andNot(ZERO), node.match(0, mask.clone()));
        assertEquals(mask.andNot(ZERO_OR_ONE), node.match(10, mask.clone()));
    }

    @Test
    public void testGreaterThanRev() {
        DoubleNode<BitsetMask> node = buildRev(100, Operation.GT);
        BitsetMask mask = FACTORY.contiguous(100);
        assertTrue(node.match(0, mask.clone()).isEmpty());
        assertEquals(ZERO, node.match(1, mask.clone()));
    }

    @Test
    public void testEqualRev() {
        DoubleNode<BitsetMask> node = buildRev(100, Operation.EQ);
        BitsetMask mask = FACTORY.contiguous(100);
        assertTrue(node.match(-1, mask.clone()).isEmpty());
        assertEquals(ZERO, node.match(0, mask.clone()));
        assertEquals(ONE, node.match(10, mask.clone()));
    }

    @Test
    public void testLessThanRev() {
        DoubleNode<BitsetMask> node = buildRev(100, Operation.LT);
        BitsetMask mask = FACTORY.contiguous(100);
        assertTrue(node.match(1001, mask.clone()).isEmpty());
        assertEquals(mask.andNot(ZERO), node.match(0, mask.clone()));
        assertEquals(mask.andNot(ZERO_OR_ONE), node.match(10, mask.clone()));
    }

    @Test
    public void testBuildNode() {
        DoubleNode<BitsetMask> node = new DoubleNode<>(FACTORY, Operation.EQ);
        node.add(0, 0);
        assertEquals(FACTORY.contiguous(1), node.match(0, FACTORY.contiguous(1)));
        node.add(0, 1);
        assertEquals(FACTORY.contiguous(2), node.match(0, FACTORY.contiguous(2)));
    }

    private DoubleNode<BitsetMask> build(int count, Operation relation) {
        DoubleNode<BitsetMask> node = new DoubleNode<>(FACTORY, relation);
        for (int i = 0; i < count; ++i) {
            node.add(i * 10, i);
        }
        return node.optimise();
    }

    private DoubleNode<BitsetMask> buildRev(int count, Operation relation) {
        DoubleNode<BitsetMask> node = new DoubleNode<>(FACTORY, relation);
        for (int i = count - 1; i >= 0; --i) {
            node.add(i * 10, i);
        }
        return node.optimise();
    }
}