package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.Operation;
import io.github.richardstartin.multimatcher.core.masks.BitsetMask;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import io.github.richardstartin.multimatcher.core.matchers.nodes.LongNode;
import org.junit.jupiter.api.Test;

import static io.github.richardstartin.multimatcher.core.Mask.with;
import static io.github.richardstartin.multimatcher.core.Mask.without;
import static io.github.richardstartin.multimatcher.core.masks.BitsetMask.store;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LongMutableNodeTest {

    @Test
    public void testGreaterThan() {
        MaskStore<BitsetMask> store = store(200);
        LongNode<BitsetMask> node = build(store,100, Operation.GT);
        int mask = store.newContiguousMaskId(100);
        assertTrue(store.isEmpty(node.match(0L, mask)));
        assertEquals(with(store.newMask(), 0), store.getMask(node.match(1L, mask)));
    }


    @Test
    public void testGreaterThanOrEqual() {
        MaskStore<BitsetMask> store = store(200);
        LongNode<BitsetMask> node = build(store,100, Operation.GE);
        int mask = store.newContiguousMaskId(100);
        assertTrue(store.isEmpty(node.match(-1L, mask)));
        assertEquals(with(store.newMask(), 0), store.getMask(node.match(0L, mask)));
        assertEquals(with(with(store.newMask(), 0), 1), store.getMask(node.match(10L, mask)));
    }

    @Test
    public void testEqual() {
        MaskStore<BitsetMask> store = store(200);
        LongNode<BitsetMask> node = build(store,100, Operation.EQ);
        int mask = store.newContiguousMaskId(100);
        assertTrue(store.isEmpty(node.match(-1L, mask)));
        assertEquals(with(store.newMask(), 0), store.getMask(node.match(0L, mask)));
        assertEquals(with(store.newMask(), 1), store.getMask(node.match(10L, mask)));
    }


    @Test
    public void testLessThanOrEqual() {
        MaskStore<BitsetMask> store = store(200);
        LongNode<BitsetMask> node = build(store,100, Operation.LE);
        int mask = store.newContiguousMaskId(100);
        assertTrue(store.isEmpty(node.match(1001, mask)));
        assertEquals(store.getMask(mask), store.getMask(node.match(0L, mask)));
        assertEquals(without(store.getMask(mask), 0), store.getMask(node.match(10L, mask)));
    }

    @Test
    public void testLessThan() {
        MaskStore<BitsetMask> store = store(200);
        LongNode<BitsetMask> node = build(store,100, Operation.LT);
        int mask = store.newContiguousMaskId(100);
        assertTrue(store.isEmpty(node.match(1001, mask)));
        assertEquals(without(store.getMask(mask).clone(), 0),
                store.getMask(node.match(0L, mask)));
        assertEquals(without(without(store.getMask(mask).clone(), 0), 1),
                store.getMask(node.match(10L, mask)));
    }

    @Test
    public void testGreaterThanRev() {
        MaskStore<BitsetMask> store = store(200);
        LongNode<BitsetMask> node = buildRev(store,100, Operation.GT);
        int mask = store.newContiguousMaskId(100);
        assertTrue(store.isEmpty(node.match(0L, mask)));
        assertEquals(with(store.newMask(), 0), store.getMask(node.match(1L, mask)));
    }

    @Test
    public void testBuildNode() {
        MaskStore<BitsetMask> store = store(200);
        LongNode<BitsetMask> node = new LongNode<>(store, Operation.EQ);
        node.add(0, 0);
        assertEquals(store.contiguous(1), store.getMask(node.match(0, store.newContiguousMaskId(1))));
        node.add(0, 1);
        assertEquals(store.contiguous(2), store.getMask(node.match(0, store.newContiguousMaskId(2))));
    }

    @Test
    public void testGreaterThanOrEqualRev() {
        MaskStore<BitsetMask> store = store(200);
        LongNode<BitsetMask> node = buildRev(store, 100, Operation.GE);
        int mask = store.newContiguousMaskId(100);
        assertTrue(store.isEmpty(node.match(-1L, mask)));
        assertEquals(with(store.newMask(), 0), store.getMask(node.match(0L, mask)));
        assertEquals(with(with(store.newMask(), 0), 1), store.getMask(node.match(10L, mask)));
    }

    @Test
    public void testEqualRev() {
        MaskStore<BitsetMask> store = store(200);
        LongNode<BitsetMask> node = buildRev(store,100, Operation.EQ);
        int mask = store.newContiguousMaskId(100);
        assertTrue(store.isEmpty(node.match(-1L, mask)));
        assertEquals(with(store.newMask(), 0), store.getMask(node.match(0L, mask)));
        assertEquals(with(store.newMask(), 1), store.getMask(node.match(10L, mask)));
    }


    @Test
    public void testLessThanOrEqualRev() {
        MaskStore<BitsetMask> store = store(200);
        LongNode<BitsetMask> node = buildRev(store,100, Operation.LE);
        int mask = store.newContiguousMaskId(100);
        assertTrue(store.isEmpty(node.match(1001L, mask)));
        assertEquals(store.getMask(mask), store.getMask(node.match(0L, mask)));
        assertEquals(without(store.getMask(mask).clone(), 0), store.getMask(node.match(10L, mask)));
    }

    @Test
    public void testLessThanRev() {
        MaskStore<BitsetMask> store = store(200);
        LongNode<BitsetMask> node = buildRev(store,100, Operation.LT);
        int mask = store.newContiguousMaskId(100);
        assertTrue(store.isEmpty(node.match(1001L, mask)));
        assertEquals(without(store.getMask(mask).clone(), 0), store.getMask(node.match(0L, mask)));
        assertEquals(without(without(store.getMask(mask).clone(), 0), 1), store.getMask(node.match(10L, mask)));
    }


    private LongNode<BitsetMask> build(MaskStore<BitsetMask> store, int count, Operation relation) {
        LongNode<BitsetMask> node = new LongNode<>(store, relation);
        for (int i = 0; i < count; ++i) {
            node.add(i * 10, i);
        }
        return node.optimise();
    }

    private LongNode<BitsetMask> buildRev(MaskStore<BitsetMask> store, int count, Operation relation) {
        LongNode<BitsetMask> node = new LongNode<>(store, relation);
        for (int i = count - 1; i >= 0; --i) {
            node.add(i * 10, i);
        }
        return node.optimise();
    }

}