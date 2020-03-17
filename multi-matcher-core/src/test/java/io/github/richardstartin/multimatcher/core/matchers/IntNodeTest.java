package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.Operation;
import io.github.richardstartin.multimatcher.core.masks.BitsetMask;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import io.github.richardstartin.multimatcher.core.matchers.nodes.IntNode;
import org.junit.jupiter.api.Test;

import static io.github.richardstartin.multimatcher.core.Mask.with;
import static io.github.richardstartin.multimatcher.core.Mask.without;
import static io.github.richardstartin.multimatcher.core.Operation.*;
import static io.github.richardstartin.multimatcher.core.masks.BitsetMask.store;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntNodeTest {


    @Test
    public void testGreaterThan() {
        MaskStore<BitsetMask> store = store(200);
        IntNode<BitsetMask> node = build(store, 100, GT);
        int mask = store.newContiguousMaskId(100);
        assertTrue(store.getMask(node.match(0, mask)).isEmpty());
        assertEquals(with(store.newMask(), 0), store.getMask(node.match(1, mask)));
    }


    @Test
    public void testGreaterThanOrEqual() {
        MaskStore<BitsetMask> store = store(200);
        IntNode<BitsetMask> node = build(store, 100, GE);
        int mask = store.newContiguousMaskId(100);
        assertTrue(store.isEmpty(node.match(-1, mask)));
        assertEquals(with(store.newMask(), 0), store.getMask(node.match(0, mask)));
        assertEquals(with(with(store.newMask(), 1), 0), store.getMask(node.match(10, mask)));
    }

    @Test
    public void testEqual() {
        MaskStore<BitsetMask> store = store(200);
        IntNode<BitsetMask> node = build(store, 100, EQ);
        int mask = store.newContiguousMaskId(100);
        assertTrue(store.isEmpty(node.match(-1, mask)));
        assertEquals(with(store.newMask(), 0), store.getMask(node.match(0, mask)));
        assertEquals(with(store.newMask(), 1), store.getMask(node.match(10, mask)));
    }


    @Test
    public void testLessThanOrEqual() {
        MaskStore<BitsetMask> store = store(200);
        IntNode<BitsetMask> node = build(store, 100, LE);
        int mask = store.newContiguousMaskId(100);
        assertTrue(store.isEmpty(node.match(1001, mask)));
        assertEquals(store.getMask(mask), store.getMask(node.match(0, mask)));
        assertEquals(without(store.getMask(mask).clone(), 0), store.getMask(node.match(10, mask)));
    }

    @Test
    public void testLessThan() {
        MaskStore<BitsetMask> store = store(200);
        IntNode<BitsetMask> node = build(store, 100, LT);
        int mask = store.newContiguousMaskId(100);
        assertTrue(store.isEmpty(node.match(1001, mask)));
        assertEquals(without(store.getMask(mask).clone(), 0), store.getMask(node.match(0, mask)));
        assertEquals(without(without(store.getMask(mask).clone(), 0), 1), store.getMask(node.match(10, mask)));
    }

    @Test
    public void testGreaterThanRev() {
        MaskStore<BitsetMask> store = store(200);
        IntNode<BitsetMask> node = buildRev(store, 100, GT);
        int mask = store.newContiguousMaskId(100);
        assertTrue(store.isEmpty(node.match(0, mask)));
        assertEquals(with(store.newMask(), 0), store.getMask(node.match(1, mask)));
    }


    @Test
    public void testGreaterThanOrEqualRev() {
        MaskStore<BitsetMask> store = store(200);
        IntNode<BitsetMask> node = buildRev(store, 100, GE);
        int mask = store.newContiguousMaskId(100);
        assertTrue(store.isEmpty(node.match(-1, mask)));
        assertEquals(with(store.newMask(), 0), store.getMask(node.match(0, mask)));
        assertEquals(with(with(store.newMask(), 0), 1), store.getMask(node.match(10, mask)));
    }

    @Test
    public void testEqualRev() {
        MaskStore<BitsetMask> factory = store(200);
        IntNode<BitsetMask> node = buildRev(factory, 100, EQ);
        int mask = factory.newContiguousMaskId(100);
        assertTrue(factory.isEmpty(node.match(-1, mask)));
        assertEquals(with(factory.newMask(), 0), factory.getMask(node.match(0, mask)));
        assertEquals(with(factory.newMask(), 1), factory.getMask(node.match(10, mask)));
    }


    @Test
    public void testLessThanOrEqualRev() {
        MaskStore<BitsetMask> factory = store(200);
        IntNode<BitsetMask> node = buildRev(factory, 100, LE);
        int mask = factory.newContiguousMaskId(100);
        assertTrue(factory.isEmpty(node.match(1001, mask)));
        assertEquals(factory.getMask(mask), factory.getMask(node.match(0, mask)));
        assertEquals(without(factory.getMask(mask).clone(), 0), factory.getMask(node.match(10, mask)));
    }

    @Test
    public void testLessThanRev() {
        MaskStore<BitsetMask> factory = store(200);
        IntNode<BitsetMask> node = buildRev(factory, 100, LT);
        int mask = factory.newContiguousMaskId(100);
        assertTrue(factory.isEmpty(node.match(1001, mask)));
        assertEquals(without(factory.getMask(mask).clone(), 0), factory.getMask(node.match(0, mask)));
        assertEquals(without(without(factory.getMask(mask).clone(), 0), 1), factory.getMask(node.match(10, mask)));
    }

    @Test
    public void testBuildNode() {
        MaskStore<BitsetMask> factory = store(200);
        IntNode<BitsetMask> node = new IntNode<>(factory, EQ);
        node.add(0, 0);
        assertEquals(factory.contiguous(1), factory.getMask(node.match(0, factory.newContiguousMaskId(1))));
        node.add(0, 1);
        assertEquals(factory.contiguous(2), factory.getMask(node.match(0, factory.newContiguousMaskId(2))));
    }

    private IntNode<BitsetMask> build(MaskStore<BitsetMask> factory, int count, Operation relation) {
        IntNode<BitsetMask> node = new IntNode<>(factory, relation);
        for (int i = 0; i < count; ++i) {
            node.add(i * 10, i);
        }
        return node.optimise();
    }

    private IntNode<BitsetMask> buildRev(MaskStore<BitsetMask> factory, int count, Operation relation) {
        IntNode<BitsetMask> node = new IntNode<>(factory, relation);
        for (int i = count - 1; i >= 0; --i) {
            node.add(i * 10, i);
        }
        return node.optimise();
    }
}