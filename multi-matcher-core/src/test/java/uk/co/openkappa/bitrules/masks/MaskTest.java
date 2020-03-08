package uk.co.openkappa.bitrules.masks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaskTest {

  @Test
  public void testTinyMask() {
    SmallMask range = SmallMask.FACTORY.contiguous(50);
    SmallMask set = SmallMask.FACTORY.of(1, 10);
    assertEquals(set, range.and(set));
    assertEquals(range, range.or(set));
    assertEquals(SmallMask.FACTORY.of(), set.andNot(range));
    assertTrue(SmallMask.FACTORY.of().isEmpty());
    assertFalse(SmallMask.FACTORY.contiguous(1).isEmpty());
  }

  @Test
  public void testStreamTinyMask() {
    assertEquals(50, SmallMask.FACTORY.contiguous(50).stream().count());
    assertEquals(50, SmallMask.FACTORY.contiguous(50).stream().distinct().count());
    assertEquals(48, SmallMask.FACTORY.contiguous(50).andNot(SmallMask.FACTORY.of(1, 2)).stream().count());
    assertEquals(48, SmallMask.FACTORY.contiguous(50).andNot(SmallMask.FACTORY.of(1, 2)).stream().distinct().count());
  }

  @Test
  public void testTinyMaskInPlace() {
    assertEquals(TinyMask.FACTORY.contiguous(11).and(TinyMask.FACTORY.of(1, 2)), TinyMask.FACTORY.contiguous(11).inPlaceAnd(TinyMask.FACTORY.of(1, 2)));
    assertEquals(TinyMask.FACTORY.contiguous(10).or(TinyMask.FACTORY.of(11, 12)), TinyMask.FACTORY.contiguous(10).inPlaceOr(TinyMask.FACTORY.of(11, 12)));
  }

  @Test
  public void testSmallMask() {
    SmallMask range = SmallMask.FACTORY.contiguous(1 << 12);
    SmallMask set = SmallMask.FACTORY.of(1, 1 << 11);
    assertEquals(set, range.and(set));
    assertEquals(range, range.or(set));
    assertEquals(SmallMask.FACTORY.of(), set.andNot(range));
    assertTrue(SmallMask.FACTORY.of().isEmpty());
    assertFalse(SmallMask.FACTORY.contiguous(1).isEmpty());
  }

  @Test
  public void testStreamSmallMask() {
    assertEquals(1 << 12, SmallMask.FACTORY.contiguous(1 << 12).stream().count());
    assertEquals(1 << 12, SmallMask.FACTORY.contiguous(1 << 12).stream().distinct().count());
    assertEquals((1 << 12) - 2, SmallMask.FACTORY.contiguous(1 << 12).andNot(SmallMask.FACTORY.of(1, 2)).stream().count());
    assertEquals((1 << 12) - 2, SmallMask.FACTORY.contiguous(1 << 12).andNot(SmallMask.FACTORY.of(1, 2)).stream().distinct().count());
  }

  @Test
  public void testSmallMaskInPlace() {
    assertEquals(SmallMask.FACTORY.contiguous(1 << 11).and(SmallMask.FACTORY.of(1, 2)), SmallMask.FACTORY.contiguous(1 << 11).inPlaceAnd(SmallMask.FACTORY.of(1, 2)));
    assertEquals(SmallMask.FACTORY.contiguous(100).or(SmallMask.FACTORY.of(101, 102)), SmallMask.FACTORY.contiguous(100).inPlaceOr(SmallMask.FACTORY.of(101, 102)));
  }

  @Test
  public void testHugeMask() {
    HugeMask range = HugeMask.FACTORY.contiguous(1 << 22);
    HugeMask set = HugeMask.FACTORY.of(1, 1 << 11);
    assertEquals(set, range.and(set));
    assertEquals(range, range.or(set));
    assertEquals(HugeMask.FACTORY.of(), set.andNot(range));
    assertTrue(HugeMask.FACTORY.of().isEmpty());
    assertFalse(HugeMask.FACTORY.contiguous(1).isEmpty());
  }

  @Test
  public void testStreamHugeMask() {
    assertEquals(1 << 22, HugeMask.FACTORY.contiguous(1 << 22).stream().count());
    assertEquals(1 << 22, HugeMask.FACTORY.contiguous(1 << 22).stream().distinct().count());
    assertEquals((1 << 22) - 2, HugeMask.FACTORY.contiguous(1 << 22).andNot(HugeMask.FACTORY.of(1, 2)).stream().count());
    assertEquals((1 << 22) - 2, HugeMask.FACTORY.contiguous(1 << 22).andNot(HugeMask.FACTORY.of(1, 2)).stream().distinct().count());
  }

  @Test
  public void testHugeMaskInPlace() {
    assertEquals(HugeMask.FACTORY.contiguous(1 << 11).and(HugeMask.FACTORY.of(1, 2)), HugeMask.FACTORY.contiguous(1 << 11).inPlaceAnd(HugeMask.FACTORY.of(1, 2)));
    assertEquals(HugeMask.FACTORY.contiguous(100).or(HugeMask.FACTORY.of(101, 102)), HugeMask.FACTORY.contiguous(100).inPlaceOr(HugeMask.FACTORY.of(101, 102)));
  }



}