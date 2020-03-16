package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.Operation;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import io.github.richardstartin.multimatcher.core.masks.WordMask;
import io.github.richardstartin.multimatcher.core.matchers.nodes.IntNode;
import org.junit.jupiter.api.Test;

import static io.github.richardstartin.multimatcher.core.Mask.with;
import static io.github.richardstartin.multimatcher.core.Mask.without;
import static io.github.richardstartin.multimatcher.core.Operation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntNodeTestTiny {

    @Test
    public void testGreaterThan() {
        var store = WordMask.store();
        IntNode<WordMask> node = build(store,5, GT);
        int mask = store.newContiguousMaskId(5);
        assertTrue(store.isEmpty(node.match(0, mask)));
        assertEquals(with(new WordMask(), 0), store.getMask(node.match(1, mask)));
    }


    @Test
    public void testGreaterThanOrEqual() {
        var store = WordMask.store();
        IntNode<WordMask> node = build(store,5, GE);
        int mask = store.newContiguousMaskId(5);
        assertTrue(store.isEmpty(node.match(-1, mask)));
        assertEquals(with(new WordMask(), 0), store.getMask(node.match(0, mask)));
        assertEquals(with(with(new WordMask(), 1), 0), store.getMask(node.match(10, mask)));
    }

    @Test
    public void testEqual() {
        var store = WordMask.store();
        IntNode<WordMask> node = build(store,5, EQ);
        int mask = store.newContiguousMaskId(5);
        assertTrue(store.isEmpty(node.match(-1, mask)));
        assertEquals(with(new WordMask(), 0), store.getMask(node.match(0, mask)));
        assertEquals(with(new WordMask(), 1), store.getMask(node.match(10, mask)));
    }


    @Test
    public void testLessThanOrEqual() {
        var store = WordMask.store();
        IntNode<WordMask> node = build(store,5, LE);
        int mask = store.newContiguousMaskId(5);
        assertTrue(store.isEmpty(node.match(1001, mask)));
        assertEquals(store.getMask(mask), store.getMask(node.match(0, mask)));
        assertEquals(without(store.getMask(mask).clone(), 0), store.getMask(node.match(10, mask)));
    }

    @Test
    public void testLessThan() {
        var store = WordMask.store();
        IntNode<WordMask> node = build(store,5, LT);
        int mask = store.newContiguousMaskId(5);
        assertTrue(store.isEmpty(node.match(1001, mask)));
        assertEquals(without(store.getMask(mask).clone(), 0), store.getMask(node.match(0, mask)));
        assertEquals(without(without(store.getMask(mask).clone(), 0), 1), store.getMask(node.match(10, mask)));
    }

    @Test
    public void testGreaterThanRev() {
        var store = WordMask.store();
        IntNode<WordMask> node = buildRev(store,5, GT);
        int mask = store.newContiguousMaskId(5);
        assertTrue(store.isEmpty(node.match(0, mask)));
        assertEquals(with(new WordMask(), 0), store.getMask(node.match(1, mask)));
    }


    @Test
    public void testGreaterThanOrEqualRev() {
        var store = WordMask.store();
        IntNode<WordMask> node = buildRev(store,5, GE);
        int mask = store.newContiguousMaskId(5);
        assertTrue(store.isEmpty(node.match(-1, mask)));
        assertEquals(with(new WordMask(), 0), store.getMask(node.match(0, mask)));
        assertEquals(with(with(new WordMask(), 0), 1), store.getMask(node.match(10, mask)));
    }

    @Test
    public void testEqualRev() {
        var store = WordMask.store();
        IntNode<WordMask> node = buildRev(store,5, EQ);
        int mask = store.newContiguousMaskId(5);
        assertTrue(store.isEmpty(node.match(-1, mask)));
        assertEquals(with(new WordMask(), 0), store.getMask(node.match(0, mask)));
        assertEquals(with(new WordMask(), 1), store.getMask(node.match(10, mask)));
    }


    @Test
    public void testLessThanOrEqualRev() {
        var store = WordMask.store();
        IntNode<WordMask> node = buildRev(store,5, LE);
        int mask = store.newContiguousMaskId(5);
        assertTrue(store.isEmpty(node.match(1001, mask)));
        assertEquals(store.getMask(mask), store.getMask(node.match(0, mask)));
        assertEquals(without(store.getMask(mask).clone(), 0), store.getMask(node.match(10, mask)));
    }

    @Test
    public void testLessThanRev() {
        var store = WordMask.store();
        IntNode<WordMask> node = buildRev(store,5, LT);
        int mask = store.newContiguousMaskId(5);
        assertTrue(store.isEmpty(node.match(1001, mask)));
        assertEquals(without(store.getMask(mask).clone(), 0), store.getMask(node.match(0, mask)));
        assertEquals(without(without(store.getMask(mask).clone(), 0), 1), store.getMask(node.match(10, mask)));
    }

    @Test
    public void testBuildNode() {
        var store = WordMask.store();
        IntNode<WordMask> node = new IntNode<>(store, EQ);
        node.add(0, 0);
        assertEquals(store.contiguous(1), store.getMask(node.match(0, store.newContiguousMaskId(1))));
        node.add(0, 1);
        assertEquals(store.contiguous(2), store.getMask(node.match(0, store.newContiguousMaskId(2))));
    }

    private IntNode<WordMask> build(MaskStore<WordMask> store, int count, Operation relation) {
        IntNode<WordMask> node = new IntNode<>(store, relation);
        for (int i = 0; i < count; ++i) {
            node.add(i * 10, i);
        }
        return node.optimise();
    }

    private IntNode<WordMask> buildRev(MaskStore<WordMask> store, int count, Operation relation) {
        IntNode<WordMask> node = new IntNode<>(store, relation);
        for (int i = count - 1; i >= 0; --i) {
            node.add(i * 10, i);
        }
        return node.optimise();
    }
}
