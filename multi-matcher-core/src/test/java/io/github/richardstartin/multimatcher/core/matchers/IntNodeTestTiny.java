package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.Operation;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import io.github.richardstartin.multimatcher.core.masks.WordMask;
import io.github.richardstartin.multimatcher.core.matchers.nodes.IntNode;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.github.richardstartin.multimatcher.core.Mask.with;
import static io.github.richardstartin.multimatcher.core.Mask.without;
import static io.github.richardstartin.multimatcher.core.Operation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class IntNodeTestTiny {

    public static Stream<Arguments> stores() {
        return Stream.of(WordMask.store(32), WordMask.store(64))
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("stores")
    public void testGreaterThan() {
        var store = WordMask.store(64);
        IntNode<WordMask> node = build(store, 5, GT);
        int mask = store.newContiguousMaskId(5);
        assertTrue(store.isEmpty(node.match(0, mask)));
        assertEquals(with(new WordMask(), 0), store.getMask(node.match(1, mask)));
    }


    @ParameterizedTest
    @MethodSource("stores")
    public void testGreaterThanOrEqual(MaskStore<WordMask> store) {
        IntNode<WordMask> node = build(store, 5, GE);
        int mask = store.newContiguousMaskId(5);
        assertTrue(store.isEmpty(node.match(-1, mask)));
        assertEquals(with(new WordMask(), 0), store.getMask(node.match(0, mask)));
        assertEquals(with(with(new WordMask(), 1), 0), store.getMask(node.match(10, mask)));
    }

    @ParameterizedTest
    @MethodSource("stores")
    public void testEqual(MaskStore<WordMask> store) {
        IntNode<WordMask> node = build(store, 5, EQ);
        int mask = store.newContiguousMaskId(5);
        assertTrue(store.isEmpty(node.match(-1, mask)));
        assertEquals(with(new WordMask(), 0), store.getMask(node.match(0, mask)));
        assertEquals(with(new WordMask(), 1), store.getMask(node.match(10, mask)));
    }


    @ParameterizedTest
    @MethodSource("stores")
    public void testLessThanOrEqual(MaskStore<WordMask> store) {
        IntNode<WordMask> node = build(store, 5, LE);
        int mask = store.newContiguousMaskId(5);
        assertTrue(store.isEmpty(node.match(1001, mask)));
        assertEquals(store.getMask(mask), store.getMask(node.match(0, mask)));
        assertEquals(without(store.getMask(mask).clone(), 0), store.getMask(node.match(10, mask)));
    }

    @ParameterizedTest
    @MethodSource("stores")
    public void testLessThan(MaskStore<WordMask> store) {
        IntNode<WordMask> node = build(store, 5, LT);
        int mask = store.newContiguousMaskId(5);
        assertTrue(store.isEmpty(node.match(1001, mask)));
        assertEquals(without(store.getMask(mask).clone(), 0), store.getMask(node.match(0, mask)));
        assertEquals(without(without(store.getMask(mask).clone(), 0), 1), store.getMask(node.match(10, mask)));
    }

    @ParameterizedTest
    @MethodSource("stores")
    public void testGreaterThanRev(MaskStore<WordMask> store) {
        IntNode<WordMask> node = buildRev(store, 5, GT);
        int mask = store.newContiguousMaskId(5);
        assertTrue(store.isEmpty(node.match(0, mask)));
        assertEquals(with(new WordMask(), 0), store.getMask(node.match(1, mask)));
    }


    @ParameterizedTest
    @MethodSource("stores")
    public void testGreaterThanOrEqualRev(MaskStore<WordMask> store) {
        IntNode<WordMask> node = buildRev(store, 5, GE);
        int mask = store.newContiguousMaskId(5);
        assertTrue(store.isEmpty(node.match(-1, mask)));
        assertEquals(with(new WordMask(), 0), store.getMask(node.match(0, mask)));
        assertEquals(with(with(new WordMask(), 0), 1), store.getMask(node.match(10, mask)));
    }

    @ParameterizedTest
    @MethodSource("stores")
    public void testEqualRev(MaskStore<WordMask> store) {
        IntNode<WordMask> node = buildRev(store, 5, EQ);
        int mask = store.newContiguousMaskId(5);
        assertTrue(store.isEmpty(node.match(-1, mask)));
        assertEquals(with(new WordMask(), 0), store.getMask(node.match(0, mask)));
        assertEquals(with(new WordMask(), 1), store.getMask(node.match(10, mask)));
    }


    @ParameterizedTest
    @MethodSource("stores")
    public void testLessThanOrEqualRev(MaskStore<WordMask> store) {
        IntNode<WordMask> node = buildRev(store, 5, LE);
        int mask = store.newContiguousMaskId(5);
        assertTrue(store.isEmpty(node.match(1001, mask)));
        assertEquals(store.getMask(mask), store.getMask(node.match(0, mask)));
        assertEquals(without(store.getMask(mask).clone(), 0), store.getMask(node.match(10, mask)));
    }

    @ParameterizedTest
    @MethodSource("stores")
    public void testLessThanRev(MaskStore<WordMask> store) {
        IntNode<WordMask> node = buildRev(store, 5, LT);
        int mask = store.newContiguousMaskId(5);
        assertTrue(store.isEmpty(node.match(1001, mask)));
        assertEquals(without(store.getMask(mask).clone(), 0), store.getMask(node.match(0, mask)));
        assertEquals(without(without(store.getMask(mask).clone(), 0), 1), store.getMask(node.match(10, mask)));
    }

    @ParameterizedTest
    @MethodSource("stores")
    public void testBuildNode(MaskStore<WordMask> store) {
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
