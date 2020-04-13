package io.github.richardstartin.multimatcher.core.matchers;


import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.Operation;
import io.github.richardstartin.multimatcher.core.masks.BitsetMask;
import io.github.richardstartin.multimatcher.core.masks.MaskStore;
import io.github.richardstartin.multimatcher.core.masks.RoaringMask;
import io.github.richardstartin.multimatcher.core.masks.WordMask;
import io.github.richardstartin.multimatcher.core.matchers.nodes.DoubleNode;
import io.github.richardstartin.multimatcher.core.matchers.nodes.LongNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.github.richardstartin.multimatcher.core.Mask.with;
import static io.github.richardstartin.multimatcher.core.Mask.without;
import static io.github.richardstartin.multimatcher.core.masks.BitsetMask.store;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class DoubleMutableNodeTest {

    public static Stream<Arguments> stores() {
        return IntStream.of(32, 64, 100, 200).boxed()
                .flatMap(i -> {
                    if (i <= 64) {
                        return Stream.of(
                                Arguments.of(i/2, WordMask.store(i)),
                                Arguments.of(i/2, BitsetMask.store(i)),
                                Arguments.of(i/2, RoaringMask.store(0, false))
                        );
                    } else {
                        return Stream.of(
                                Arguments.of(i/2, BitsetMask.store(i)),
                                Arguments.of(i/2, RoaringMask.store(0, false))
                        );
                    }
                });
    }

    @ParameterizedTest
    @MethodSource("stores")
    public <MaskType extends Mask<MaskType>> void testGreaterThan(int maxElement, MaskStore<MaskType> store) {
        var node = build(store, maxElement, Operation.GT);
        int mask = store.newContiguousMaskId(maxElement);
        assertTrue(store.isEmpty(node.match(0L, mask)));
        assertEquals(with(store.newMask(), 0), store.getMask(node.match(1L, mask)));
    }


    @ParameterizedTest
    @MethodSource("stores")
    public <MaskType extends Mask<MaskType>> void testGreaterThanOrEqual(int maxElement, MaskStore<MaskType> store) {
        var node = build(store, maxElement, Operation.GE);
        int mask = store.newContiguousMaskId(maxElement);
        assertTrue(store.isEmpty(node.match(-1L, mask)));
        assertEquals(with(store.newMask(), 0), store.getMask(node.match(0L, mask)));
        assertEquals(with(with(store.newMask(), 0), 1), store.getMask(node.match(10L, mask)));
    }

    @ParameterizedTest
    @MethodSource("stores")
    public <MaskType extends Mask<MaskType>> void testEqual(int maxElement, MaskStore<MaskType> store) {
        var node = build(store, maxElement, Operation.EQ);
        int mask = store.newContiguousMaskId(maxElement);
        assertTrue(store.isEmpty(node.match(-1L, mask)));
        assertEquals(with(store.newMask(), 0), store.getMask(node.match(0L, mask)));
        assertEquals(with(store.newMask(), 1), store.getMask(node.match(10L, mask)));
    }


    @ParameterizedTest
    @MethodSource("stores")
    public <MaskType extends Mask<MaskType>> void testLessThanOrEqual(int maxElement, MaskStore<MaskType> store) {
        var node = build(store, maxElement, Operation.LE);
        int mask = store.newContiguousMaskId(maxElement);
        assertTrue(store.isEmpty(node.match(1001, mask)));
        assertEquals(store.getMask(mask), store.getMask(node.match(0L, mask)));
        assertEquals(without(store.getMask(mask), 0), store.getMask(node.match(10L, mask)));
    }

    @ParameterizedTest
    @MethodSource("stores")
    public <MaskType extends Mask<MaskType>> void testLessThan(int maxElement, MaskStore<MaskType> store) {
        var node = build(store, maxElement, Operation.LT);
        int mask = store.newContiguousMaskId(maxElement);
        assertTrue(store.isEmpty(node.match(1001, mask)));
        assertEquals(without(store.getMask(mask).clone(), 0),
                store.getMask(node.match(0L, mask)));
        assertEquals(without(without(store.getMask(mask).clone(), 0), 1),
                store.getMask(node.match(10L, mask)));
    }

    @ParameterizedTest
    @MethodSource("stores")
    public <MaskType extends Mask<MaskType>> void testGreaterThanRev(int maxElement, MaskStore<MaskType> store) {
        var node = buildRev(store, maxElement, Operation.GT);
        int mask = store.newContiguousMaskId(maxElement);
        assertTrue(store.isEmpty(node.match(0L, mask)));
        assertEquals(with(store.newMask(), 0), store.getMask(node.match(1L, mask)));
    }

    @ParameterizedTest
    @MethodSource("stores")
    public <MaskType extends Mask<MaskType>> void testBuildNode(int maxElement, MaskStore<MaskType> store) {
        var node = new LongNode<>(store, Operation.EQ);
        node.add(0, 0);
        assertEquals(store.contiguous(1), store.getMask(node.match(0, store.newContiguousMaskId(1))));
        node.add(0, 1);
        assertEquals(store.contiguous(2), store.getMask(node.match(0, store.newContiguousMaskId(2))));
    }

    @ParameterizedTest
    @MethodSource("stores")
    public <MaskType extends Mask<MaskType>> void testGreaterThanOrEqualRev(int maxElement, MaskStore<MaskType> store) {
        var node = buildRev(store, maxElement, Operation.GE);
        int mask = store.newContiguousMaskId(maxElement);
        assertTrue(store.isEmpty(node.match(-1L, mask)));
        assertEquals(with(store.newMask(), 0), store.getMask(node.match(0L, mask)));
        assertEquals(with(with(store.newMask(), 0), 1), store.getMask(node.match(10L, mask)));
    }

    @ParameterizedTest
    @MethodSource("stores")
    public <MaskType extends Mask<MaskType>> void testEqualRev(int maxElement, MaskStore<MaskType> store) {
        var node = buildRev(store, maxElement, Operation.EQ);
        int mask = store.newContiguousMaskId(maxElement);
        assertTrue(store.isEmpty(node.match(-1L, mask)));
        assertEquals(with(store.newMask(), 0), store.getMask(node.match(0L, mask)));
        assertEquals(with(store.newMask(), 1), store.getMask(node.match(10L, mask)));
    }


    @ParameterizedTest
    @MethodSource("stores")
    public <MaskType extends Mask<MaskType>> void testLessThanOrEqualRev(int maxElement, MaskStore<MaskType> store) {
        var node = buildRev(store, maxElement, Operation.LE);
        int mask = store.newContiguousMaskId(maxElement);
        assertTrue(store.isEmpty(node.match(1001L, mask)));
        assertEquals(store.getMask(mask), store.getMask(node.match(0L, mask)));
        assertEquals(without(store.getMask(mask).clone(), 0), store.getMask(node.match(10L, mask)));
    }

    @ParameterizedTest
    @MethodSource("stores")
    public <MaskType extends Mask<MaskType>> void testLessThanRev(int maxElement, MaskStore<MaskType> store) {
        var node = buildRev(store, maxElement, Operation.LT);
        int mask = store.newContiguousMaskId(maxElement);
        assertTrue(store.isEmpty(node.match(1001L, mask)));
        assertEquals(without(store.getMask(mask).clone(), 0), store.getMask(node.match(0L, mask)));
        assertEquals(without(without(store.getMask(mask).clone(), 0), 1), store.getMask(node.match(10L, mask)));
    }


    private <MaskType extends Mask<MaskType>>
    DoubleNode<MaskType> build(MaskStore<MaskType> store, int count, Operation relation) {
        DoubleNode<MaskType> node = new DoubleNode<>(store, relation);
        for (int i = 0; i < count; ++i) {
            node.add(i * 10, i);
        }
        return node.optimise();
    }

    private <MaskType extends Mask<MaskType>>
    DoubleNode<MaskType> buildRev(MaskStore<MaskType> store, int count, Operation relation) {
        DoubleNode<MaskType> node = new DoubleNode<>(store, relation);
        for (int i = count - 1; i >= 0; --i) {
            node.add(i * 10, i);
        }
        return node.optimise();
    }
}