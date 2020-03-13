package io.github.richardstartin.multimatcher.core.masks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaskTest {
  
  private final BitmapMask.Factory BITMAP_MASK_FACTORY = BitmapMask.factory(1 << 12);

  @Test
  public void testTinyMask() {
    var range = TinyMask.FACTORY.contiguous(50);
    var set = TinyMask.FACTORY.of(1, 10);
    assertEquals(set, range.and(set));
    assertEquals(range, range.or(set));
    assertEquals(TinyMask.FACTORY.of(), set.andNot(range));
    assertTrue(TinyMask.FACTORY.of().isEmpty());
    assertFalse(TinyMask.FACTORY.contiguous(1).isEmpty());
  }

  @Test
  public void testStreamTinyMask() {
    assertEquals(50, TinyMask.FACTORY.contiguous(50).stream().count());
    assertEquals(50, TinyMask.FACTORY.contiguous(50).stream().distinct().count());
    assertEquals(48, TinyMask.FACTORY.contiguous(50).inPlaceAndNot(TinyMask.FACTORY.of(1, 2)).stream().count());
    assertEquals(48, TinyMask.FACTORY.contiguous(50).inPlaceAndNot(TinyMask.FACTORY.of(1, 2)).stream().distinct().count());
  }

  @Test
  public void testTinyMaskInPlace() {
    assertEquals(TinyMask.FACTORY.contiguous(11).inPlaceAnd(TinyMask.FACTORY.of(1, 2)), TinyMask.FACTORY.contiguous(11).inPlaceAnd(TinyMask.FACTORY.of(1, 2)));
    assertEquals(TinyMask.FACTORY.contiguous(10).inPlaceOr(TinyMask.FACTORY.of(11, 12)), TinyMask.FACTORY.contiguous(10).inPlaceOr(TinyMask.FACTORY.of(11, 12)));
  }

  @Test
  public void testBitmapMask() {
    BitmapMask range = BITMAP_MASK_FACTORY.contiguous(1 << 12);
    BitmapMask set = BITMAP_MASK_FACTORY.of(1, 1 << 11);
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
    RoaringMask range = RoaringMask.FACTORY.contiguous(1 << 22);
    RoaringMask set = RoaringMask.FACTORY.of(1, 1 << 11);
    assertEquals(set, range.and(set));
    assertEquals(range, range.or(set));
    assertEquals(RoaringMask.FACTORY.of(), set.andNot(range));
    assertTrue(RoaringMask.FACTORY.of().isEmpty());
    assertFalse(RoaringMask.FACTORY.contiguous(1).isEmpty());
  }

  @Test
  public void testStreamHugeMask() {
    assertEquals(1 << 22, RoaringMask.FACTORY.contiguous(1 << 22).stream().count());
    assertEquals(1 << 22, RoaringMask.FACTORY.contiguous(1 << 22).stream().distinct().count());
    assertEquals((1 << 22) - 2, RoaringMask.FACTORY.contiguous(1 << 22).andNot(RoaringMask.FACTORY.of(1, 2)).stream().count());
    assertEquals((1 << 22) - 2, RoaringMask.FACTORY.contiguous(1 << 22).andNot(RoaringMask.FACTORY.of(1, 2)).stream().distinct().count());
  }

  @Test
  public void testHugeMaskInPlace() {
    assertEquals(RoaringMask.FACTORY.contiguous(1 << 11).and(RoaringMask.FACTORY.of(1, 2)), RoaringMask.FACTORY.contiguous(1 << 11).inPlaceAnd(RoaringMask.FACTORY.of(1, 2)));
    assertEquals(RoaringMask.FACTORY.contiguous(100).or(RoaringMask.FACTORY.of(101, 102)), RoaringMask.FACTORY.contiguous(100).inPlaceOr(RoaringMask.FACTORY.of(101, 102)));
  }



}