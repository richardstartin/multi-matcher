package io.github.richardstartin.multimatcher.core.matchers;


import io.github.richardstartin.multimatcher.core.Operation;
import io.github.richardstartin.multimatcher.core.masks.BitsetMask;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import io.github.richardstartin.multimatcher.core.matchers.nodes.ComparableNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.LocalDate;
import java.util.Comparator;

import static io.github.richardstartin.multimatcher.core.Mask.with;
import static io.github.richardstartin.multimatcher.core.Operation.*;
import static io.github.richardstartin.multimatcher.core.masks.BitsetMask.store;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class ComparableMutableNodeTest {

    private MaskStore<BitsetMask> store;
    private BitsetMask zero;
    private BitsetMask one;
    private BitsetMask zeroOrOne;

    @BeforeEach
    public void init() {
        store = store(200);
        zero = with(store.newMask(), 0);
        one = with(store.newMask(), 1);
        zeroOrOne = zero.or(one);
    }

    @Test
    public void testGreaterThan() {
        var node = build(100, GT);
        assertTrue(store.isEmpty(node.match(LocalDate.ofEpochDay(0))));
        assertEquals(zero, store.getMask(node.match(LocalDate.ofEpochDay(1))));
        assertEquals(zeroOrOne, store.getMask(node.match(LocalDate.ofEpochDay(11))));
    }

    @Test
    public void testEqual() {
        var node = build(100, EQ);
        assertTrue(store.isEmpty(node.match(LocalDate.ofEpochDay(1))));
        assertEquals(zero, store.getMask(node.match(LocalDate.ofEpochDay(0))));
        assertEquals(one, store.getMask(node.match(LocalDate.ofEpochDay(10))));
    }

    @Test
    public void testLessThan() {
        var node = build(100, LT);
        BitsetMask mask = store.contiguous(100);
        assertTrue(store.isEmpty(node.match(LocalDate.ofEpochDay(1001))));
        assertEquals(mask.andNot(zero), store.getMask(node.match(LocalDate.ofEpochDay(0))));
        assertEquals(mask.andNot(zeroOrOne), store.getMask(node.match(LocalDate.ofEpochDay(10))));
    }

    @Test
    public void testGreaterThanRev() {
        var node = buildRev(100, GT);
        assertTrue(store.isEmpty(node.match(LocalDate.ofEpochDay(0))));
        assertEquals(zero, store.getMask(node.match(LocalDate.ofEpochDay(1))));
    }

    @Test
    public void testEqualRev() {
        var node = buildRev(100, EQ);
        assertTrue(store.isEmpty(node.match(LocalDate.ofEpochDay(1))));
        assertEquals(zero, store.getMask(node.match(LocalDate.ofEpochDay(0))));
        assertEquals(one, store.getMask(node.match(LocalDate.ofEpochDay(10))));
    }

    @Test
    public void testLessThanRev() {
        var node = buildRev(100, LT);
        BitsetMask mask = store.contiguous(100);
        assertTrue(store.isEmpty(node.match(LocalDate.ofEpochDay(1001))));
        assertEquals(mask.andNot(zero), store.getMask(node.match(LocalDate.ofEpochDay(0))));
        assertEquals(mask.andNot(zeroOrOne), store.getMask(node.match(LocalDate.ofEpochDay(10))));
    }

    @Test
    public void testBuildNode() {
        var node = new ComparableNode<>(store, Comparator.comparingDouble(Double::doubleValue), GT);
        node.add(0D, 0);
        assertEquals(store.contiguous(1), store.getMask(node.match(1D)));
        node.add(10D, 1);
        node.freeze();
        assertEquals(store.contiguous(2), store.getMask(node.match(11D)));
    }

    private ComparableNode<LocalDate, BitsetMask> build(int count, Operation operation) {
        var node = new ComparableNode<>(store, Comparator.<LocalDate>naturalOrder(), operation);
        for (int i = 0; i < count; ++i) {
            node.add(LocalDate.ofEpochDay(i * 10), i);
        }
        return node.freeze();
    }

    private ComparableNode<LocalDate, BitsetMask> buildRev(int count, Operation operation) {
        ComparableNode<LocalDate, BitsetMask> node = new ComparableNode<>(store, Comparator.<LocalDate>naturalOrder(), operation);
        for (int i = count - 1; i >= 0; --i) {
            node.add(LocalDate.ofEpochDay(i * 10), i);
        }
        return node.freeze();
    }
}