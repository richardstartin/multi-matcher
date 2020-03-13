package io.github.richardstartin.multimatcher.core.masks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaskTest {
  
  private final BitmapMask.Factory BITMAP_MASK_FACTORY = BitmapMask.factory(1 << 12);
  private final MaskFactory<RoaringMask> ROARING_MASK_FACTORY = RoaringMask.factory(1024 * 1024, false);

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