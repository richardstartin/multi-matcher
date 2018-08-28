package uk.co.openkappa.bitrules.masks;

import org.junit.jupiter.api.Test;
import uk.co.openkappa.bitrules.masks.HugeMask;
import uk.co.openkappa.bitrules.masks.SmallMask;
import uk.co.openkappa.bitrules.masks.TinyMask;

import static org.junit.jupiter.api.Assertions.*;

class MaskTest {

  @Test
  public void testTinyMask() {
    SmallMask range = SmallMask.contiguous(50);
    SmallMask set = SmallMask.of(1, 10);
    assertEquals(set, range.and(set));
    assertEquals(range, range.or(set));
    assertEquals(SmallMask.of(), set.andNot(range));
    assertTrue(SmallMask.of().isEmpty());
    assertFalse(SmallMask.contiguous(1).isEmpty());
  }

  @Test
  public void testStreamTinyMask() {
    assertEquals(50, SmallMask.contiguous(50).stream().count());
    assertEquals(50, SmallMask.contiguous(50).stream().distinct().count());
    assertEquals(48, SmallMask.contiguous(50).andNot(SmallMask.of(1, 2)).stream().count());
    assertEquals(48, SmallMask.contiguous(50).andNot(SmallMask.of(1, 2)).stream().distinct().count());
  }

  @Test
  public void testTinyMaskInPlace() {
    assertEquals(TinyMask.contiguous(11).and(TinyMask.of(1, 2)), TinyMask.contiguous(11).inPlaceAnd(TinyMask.of(1, 2)));
    assertEquals(TinyMask.contiguous(10).or(TinyMask.of(11, 12)), TinyMask.contiguous(10).inPlaceOr(TinyMask.of(11, 12)));
  }

  @Test
  public void testSmallMask() {
    SmallMask range = SmallMask.contiguous(1 << 12);
    SmallMask set = SmallMask.of(1, 1 << 11);
    assertEquals(set, range.and(set));
    assertEquals(range, range.or(set));
    assertEquals(SmallMask.of(), set.andNot(range));
    assertTrue(SmallMask.of().isEmpty());
    assertFalse(SmallMask.contiguous(1).isEmpty());
  }

  @Test
  public void testStreamSmallMask() {
    assertEquals(1 << 12, SmallMask.contiguous(1 << 12).stream().count());
    assertEquals(1 << 12, SmallMask.contiguous(1 << 12).stream().distinct().count());
    assertEquals((1 << 12) - 2, SmallMask.contiguous(1 << 12).andNot(SmallMask.of(1, 2)).stream().count());
    assertEquals((1 << 12) - 2, SmallMask.contiguous(1 << 12).andNot(SmallMask.of(1, 2)).stream().distinct().count());
  }

  @Test
  public void testSmallMaskInPlace() {
    assertEquals(SmallMask.contiguous(1 << 11).and(SmallMask.of(1, 2)), SmallMask.contiguous(1 << 11).inPlaceAnd(SmallMask.of(1, 2)));
    assertEquals(SmallMask.contiguous(100).or(SmallMask.of(101, 102)), SmallMask.contiguous(100).inPlaceOr(SmallMask.of(101, 102)));
  }

  @Test
  public void testHugeMask() {
    HugeMask range = HugeMask.contiguous(1 << 22);
    HugeMask set = HugeMask.of(1, 1 << 11);
    assertEquals(set, range.and(set));
    assertEquals(range, range.or(set));
    assertEquals(HugeMask.of(), set.andNot(range));
    assertTrue(HugeMask.of().isEmpty());
    assertFalse(HugeMask.contiguous(1).isEmpty());
  }

  @Test
  public void testStreamHugeMask() {
    assertEquals(1 << 22, HugeMask.contiguous(1 << 22).stream().count());
    assertEquals(1 << 22, HugeMask.contiguous(1 << 22).stream().distinct().count());
    assertEquals((1 << 22) - 2, HugeMask.contiguous(1 << 22).andNot(HugeMask.of(1, 2)).stream().count());
    assertEquals((1 << 22) - 2, HugeMask.contiguous(1 << 22).andNot(HugeMask.of(1, 2)).stream().distinct().count());
  }

  @Test
  public void testHugeMaskInPlace() {
    assertEquals(HugeMask.contiguous(1 << 11).and(HugeMask.of(1, 2)), HugeMask.contiguous(1 << 11).inPlaceAnd(HugeMask.of(1, 2)));
    assertEquals(HugeMask.contiguous(100).or(HugeMask.of(101, 102)), HugeMask.contiguous(100).inPlaceOr(HugeMask.of(101, 102)));
  }



}