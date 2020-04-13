package io.github.richardstartin.multimatcher.core.masks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
class MaskTest {

    private MaskStore<BitsetMask> bitmapMaskStore;
    private MaskStore<RoaringMask> roaringMaskStore;
    private MaskStore<WordMask> wordMaskStore;

    @BeforeEach
    public void init() {
        bitmapMaskStore = BitsetMask.store(1 << 12);
        roaringMaskStore = RoaringMask.store(1024 * 1024, false);
        wordMaskStore = WordMask.store(64);
    }

    @Test
    public void testTinyMask() {
        var range = wordMaskStore.contiguous(50);
        var set = wordMaskStore.of(1, 10);
        assertEquals(set, range.and(set));
        assertEquals(range, range.or(set));
        assertEquals(wordMaskStore.of(), set.andNot(range));
        assertTrue(wordMaskStore.of().isEmpty());
        assertFalse(wordMaskStore.contiguous(1).isEmpty());
    }

    @Test
    public void testStreamTinyMask() {
        assertEquals(50, wordMaskStore.contiguous(50).stream().count());
        assertEquals(50, wordMaskStore.contiguous(50).stream().distinct().count());
        assertEquals(48, wordMaskStore.contiguous(50).inPlaceAndNot(wordMaskStore.of(1, 2)).stream().count());
        assertEquals(48, wordMaskStore.contiguous(50).inPlaceAndNot(wordMaskStore.of(1, 2)).stream().distinct().count());
    }

    @Test
    public void testTinyMaskInPlace() {
        assertEquals(wordMaskStore.contiguous(11).inPlaceAnd(wordMaskStore.of(1, 2)), wordMaskStore.contiguous(11).inPlaceAnd(wordMaskStore.of(1, 2)));
        assertEquals(wordMaskStore.contiguous(10).inPlaceOr(wordMaskStore.of(11, 12)), wordMaskStore.contiguous(10).inPlaceOr(wordMaskStore.of(11, 12)));
    }

    @Test
    public void testBitmapMask() {
        BitsetMask range = bitmapMaskStore.contiguous(1 << 12);
        BitsetMask set = bitmapMaskStore.of(1, 1 << 11);
        assertEquals(set, range.and(set));
        assertEquals(range, range.or(set));
        assertEquals(bitmapMaskStore.of(), set.andNot(range));
        assertTrue(bitmapMaskStore.of().isEmpty());
        assertFalse(bitmapMaskStore.contiguous(1).isEmpty());
    }

    @Test
    public void testStreamBitmapMask() {
        assertEquals(1 << 12, bitmapMaskStore.contiguous(1 << 12).stream().count());
        assertEquals(1 << 12, bitmapMaskStore.contiguous(1 << 12).stream().distinct().count());
        assertEquals((1 << 12) - 2, bitmapMaskStore.contiguous(1 << 12).inPlaceAndNot(bitmapMaskStore.of(1, 2)).stream().count());
        assertEquals((1 << 12) - 2, bitmapMaskStore.contiguous(1 << 12).inPlaceAndNot(bitmapMaskStore.of(1, 2)).stream().distinct().count());
    }

    @Test
    public void testBitmapMaskInPlace() {
        assertEquals(bitmapMaskStore.contiguous(1 << 11).and(bitmapMaskStore.of(1, 2)), bitmapMaskStore.contiguous(1 << 11).inPlaceAnd(bitmapMaskStore.of(1, 2)));
        assertEquals(bitmapMaskStore.contiguous(100).or(bitmapMaskStore.of(101, 102)), bitmapMaskStore.contiguous(100).inPlaceOr(bitmapMaskStore.of(101, 102)));
    }

    @Test
    public void testHugeMask() {
        RoaringMask range = roaringMaskStore.contiguous(1 << 22);
        RoaringMask set = roaringMaskStore.of(1, 1 << 11);
        assertEquals(set, range.and(set));
        assertEquals(range, range.or(set));
        assertEquals(roaringMaskStore.of(), set.andNot(range));
        assertTrue(roaringMaskStore.of().isEmpty());
        assertFalse(roaringMaskStore.contiguous(1).isEmpty());
    }


    @Test
    public void testOptimiseRoaringMask() {
        RoaringMask range = roaringMaskStore.contiguous(1 << 22);
        range.optimise();
        RoaringMask set = roaringMaskStore.of(1, 1 << 11);
        assertEquals(set, range.and(set));
        assertEquals(range, range.or(set));
        assertEquals(roaringMaskStore.of(), set.andNot(range));
        assertTrue(roaringMaskStore.of().isEmpty());
        assertFalse(roaringMaskStore.contiguous(1).isEmpty());
    }

    @Test
    public void testStreamHugeMask() {
        assertEquals(1 << 22, roaringMaskStore.contiguous(1 << 22).stream().count());
        assertEquals(1 << 22, roaringMaskStore.contiguous(1 << 22).stream().distinct().count());
        assertEquals((1 << 22) - 2, roaringMaskStore.contiguous(1 << 22).andNot(roaringMaskStore.of(1, 2)).stream().count());
        assertEquals((1 << 22) - 2, roaringMaskStore.contiguous(1 << 22).andNot(roaringMaskStore.of(1, 2)).stream().distinct().count());
    }

    @Test
    public void testHugeMaskInPlace() {
        assertEquals(roaringMaskStore.contiguous(1 << 11).and(roaringMaskStore.of(1, 2)), roaringMaskStore.contiguous(1 << 11).inPlaceAnd(roaringMaskStore.of(1, 2)));
        assertEquals(roaringMaskStore.contiguous(100).or(roaringMaskStore.of(101, 102)), roaringMaskStore.contiguous(100).inPlaceOr(roaringMaskStore.of(101, 102)));
    }


}