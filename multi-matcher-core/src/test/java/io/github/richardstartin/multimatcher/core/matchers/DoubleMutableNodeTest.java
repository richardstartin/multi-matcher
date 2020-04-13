package io.github.richardstartin.multimatcher.core.matchers;


import io.github.richardstartin.multimatcher.core.Operation;
import io.github.richardstartin.multimatcher.core.masks.BitsetMask;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import io.github.richardstartin.multimatcher.core.matchers.nodes.DoubleNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static io.github.richardstartin.multimatcher.core.Mask.with;
import static io.github.richardstartin.multimatcher.core.masks.BitsetMask.store;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class DoubleMutableNodeTest {

    private MaskStore<BitsetMask> store;
    private BitsetMask zero;
    private BitsetMask one;
    private BitsetMask zeroOrOne;


    @BeforeEach
    public void setup() {
        store = store(200);
        zero = with(store.newMask(), 0);
        one = with(store.newMask(), 1);
        zeroOrOne = zero.or(one);
    }

    @Test
    public void testGreaterThan() {
        DoubleNode<BitsetMask> node = build(100, Operation.GT);
        int mask = store.newContiguousMaskId(100);
        assertTrue(store.isEmpty(node.match(0, mask)));
        assertEquals(zero, store.getMask(node.match(1, mask)));
        assertEquals(zeroOrOne, store.getMask(node.match(11, mask)));
    }

    @Test
    public void testEqual() {
        DoubleNode<BitsetMask> node = build(100, Operation.EQ);
        int mask = store.newContiguousMaskId(100);
        assertTrue(store.isEmpty(node.match(-1L, mask)));
        assertEquals(zero, store.getMask(node.match(0, mask)));
        assertEquals(one, store.getMask(node.match(10, mask)));
    }

    @Test
    public void testLessThan() {
        DoubleNode<BitsetMask> node = build(100, Operation.LT);
        int mask = store.newContiguousMaskId(100);
        assertTrue(store.isEmpty(node.match(1001, mask)));
        assertEquals(store.getMask(mask).andNot(zero), store.getMask(node.match(0, mask)));
        assertEquals(store.getMask(mask).andNot(zeroOrOne), store.getMask(node.match(10, mask)));
    }

    @Test
    public void testGreaterThanRev() {
        DoubleNode<BitsetMask> node = buildRev(100, Operation.GT);
        int mask = store.newContiguousMaskId(100);
        assertTrue(store.isEmpty(node.match(0, mask)));
        assertEquals(zero, store.getMask(node.match(1, mask)));
    }

    @Test
    public void testEqualRev() {
        DoubleNode<BitsetMask> node = buildRev(100, Operation.EQ);
        int mask = store.newContiguousMaskId(100);
        assertTrue(store.isEmpty(node.match(-1, mask)));
        assertEquals(zero, store.getMask(node.match(0, mask)));
        assertEquals(one, store.getMask(node.match(10, mask)));
    }

    @Test
    public void testLessThanRev() {
        DoubleNode<BitsetMask> node = buildRev(100, Operation.LT);
        int mask = store.newContiguousMaskId(100);
        assertTrue(store.isEmpty(node.match(1001, mask)));
        assertEquals(store.getMask(mask).andNot(zero), store.getMask(node.match(0, mask)));
        assertEquals(store.getMask(mask).andNot(zeroOrOne), store.getMask(node.match(10, mask)));
    }

    @Test
    public void testBuildNode() {
        DoubleNode<BitsetMask> node = new DoubleNode<>(store, Operation.EQ);
        node.add(0, 0);
        assertEquals(store.contiguous(1), store.getMask(node.match(0, store.newContiguousMaskId(1))));
        node.add(0, 1);
        assertEquals(store.contiguous(2), store.getMask(node.match(0, store.newContiguousMaskId(2))));
    }

    private DoubleNode<BitsetMask> build(int count, Operation relation) {
        DoubleNode<BitsetMask> node = new DoubleNode<>(store, relation);
        for (int i = 0; i < count; ++i) {
            node.add(i * 10, i);
        }
        return node.optimise();
    }

    private DoubleNode<BitsetMask> buildRev(int count, Operation relation) {
        DoubleNode<BitsetMask> node = new DoubleNode<>(store, relation);
        for (int i = count - 1; i >= 0; --i) {
            node.add(i * 10, i);
        }
        return node.optimise();
    }
}