package io.github.richardstartin.multimatcher.core.masks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaskTest {
  
  private final MaskFactory<BitsetMask> BITMAP_MASK_FACTORY = BitsetMask.factory(1 << 12);
  private final MaskFactory<RoaringMask> ROARING_MASK_FACTORY = RoaringMask.factory(1024 * 1024, false);

  @Test
  public void testTinyMask() {
    var range = WordMask.FACTORY.contiguous(50);
    var set = WordMask.FACTORY.of(1, 10);
    assertEquals(set, range.and(set));
    assertEquals(range, range.or(set));
    assertEquals(WordMask.FACTORY.of(), set.andNot(range));
    assertTrue(WordMask.FACTORY.of().isEmpty());
    assertFalse(WordMask.FACTORY.contiguous(1).isEmpty());
  }

  @Test
  public void testStreamTinyMask() {
    assertEquals(50, WordMask.FACTORY.contiguous(50).stream().count());
    assertEquals(50, WordMask.FACTORY.contiguous(50).stream().distinct().count());
    assertEquals(48, WordMask.FACTORY.contiguous(50).inPlaceAndNot(WordMask.FACTORY.of(1, 2)).stream().count());
    assertEquals(48, WordMask.FACTORY.contiguous(50).inPlaceAndNot(WordMask.FACTORY.of(1, 2)).stream().distinct().count());
  }

  @Test
  public void testTinyMaskInPlace() {
    assertEquals(WordMask.FACTORY.contiguous(11).inPlaceAnd(WordMask.FACTORY.of(1, 2)), WordMask.FACTORY.contiguous(11).inPlaceAnd(WordMask.FACTORY.of(1, 2)));
    assertEquals(WordMask.FACTORY.contiguous(10).inPlaceOr(WordMask.FACTORY.of(11, 12)), WordMask.FACTORY.contiguous(10).inPlaceOr(WordMask.FACTORY.of(11, 12)));
  }

  @Test
  public void testBitmapMask() {
    BitsetMask range = BITMAP_MASK_FACTORY.contiguous(1 << 12);
    BitsetMask set = BITMAP_MASK_FACTORY.of(1, 1 << 11);
    assertEquals(set, range.and(set));
    assertEquals(range, range.or(set));
    assertEquals(BITMAP_MASK_FACTORY.of(), set.andNot(range));
    assertTrue(BITMAP_MASK_FACTORY.of().isEmpty());
    assertFalse(BITMAP_MASK_FACTORY.contiguous(1).isEmpty());
  }

  @Test
  public void testStreamBitmapMask() {
    assertEquals(1 << 12, BITMAP_MASK_FACTORY.contiguous(1 << 12).stream().count());
    assertEquals(1 << 12, BITMAP_MASK_FACTORY.contiguous(1 << 12).stream().distinct().count());
    assertEquals((1 << 12) - 2, BITMAP_MASK_FACTORY.contiguous(1 << 12).inPlaceAndNot(BITMAP_MASK_FACTORY.of(1, 2)).stream().count());
    assertEquals((1 << 12) - 2, BITMAP_MASK_FACTORY.contiguous(1 << 12).inPlaceAndNot(BITMAP_MASK_FACTORY.of(1, 2)).stream().distinct().count());
  }

  @Test
  public void testBitmapMaskInPlace() {
    assertEquals(BITMAP_MASK_FACTORY.contiguous(1 << 11).and(BITMAP_MASK_FACTORY.of(1, 2)), BITMAP_MASK_FACTORY.contiguous(1 << 11).inPlaceAnd(BITMAP_MASK_FACTORY.of(1, 2)));
    assertEquals(BITMAP_MASK_FACTORY.contiguous(100).or(BITMAP_MASK_FACTORY.of(101, 102)), BITMAP_MASK_FACTORY.contiguous(100).inPlaceOr(BITMAP_MASK_FACTORY.of(101, 102)));
  }

  @Test
  public void testHugeMask() {
    RoaringMask range = ROARING_MASK_FACTORY.contiguous(1 << 22);
    RoaringMask set = ROARING_MASK_FACTORY.of(1, 1 << 11);
    assertEquals(set, range.and(set));
    assertEquals(range, range.or(set));
    assertEquals(ROARING_MASK_FACTORY.of(), set.andNot(range));
    assertTrue(ROARING_MASK_FACTORY.of().isEmpty());
    assertFalse(ROARING_MASK_FACTORY.contiguous(1).isEmpty());
  }



  @Test
  public void testOptimiseRoaringMask() {
    RoaringMask range = ROARING_MASK_FACTORY.contiguous(1 << 22);
    range.optimise();
    RoaringMask set = ROARING_MASK_FACTORY.of(1, 1 << 11);
    assertEquals(set, range.and(set));
    assertEquals(range, range.or(set));
    assertEquals(ROARING_MASK_FACTORY.of(), set.andNot(range));
    assertTrue(ROARING_MASK_FACTORY.of().isEmpty());
    assertFalse(ROARING_MASK_FACTORY.contiguous(1).isEmpty());
  }

  @Test
  public void testStreamHugeMask() {
    assertEquals(1 << 22, ROARING_MASK_FACTORY.contiguous(1 << 22).stream().count());
    assertEquals(1 << 22, ROARING_MASK_FACTORY.contiguous(1 << 22).stream().distinct().count());
    assertEquals((1 << 22) - 2, ROARING_MASK_FACTORY.contiguous(1 << 22).andNot(ROARING_MASK_FACTORY.of(1, 2)).stream().count());
    assertEquals((1 << 22) - 2, ROARING_MASK_FACTORY.contiguous(1 << 22).andNot(ROARING_MASK_FACTORY.of(1, 2)).stream().distinct().count());
  }

  @Test
  public void testHugeMaskInPlace() {
    assertEquals(ROARING_MASK_FACTORY.contiguous(1 << 11).and(ROARING_MASK_FACTORY.of(1, 2)), ROARING_MASK_FACTORY.contiguous(1 << 11).inPlaceAnd(ROARING_MASK_FACTORY.of(1, 2)));
    assertEquals(ROARING_MASK_FACTORY.contiguous(100).or(ROARING_MASK_FACTORY.of(101, 102)), ROARING_MASK_FACTORY.contiguous(100).inPlaceOr(ROARING_MASK_FACTORY.of(101, 102)));
  }



}