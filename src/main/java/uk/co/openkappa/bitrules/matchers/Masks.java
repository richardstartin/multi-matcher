package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.ContainerMask;
import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.RoaringBitmapMask;
import uk.co.openkappa.bitrules.WordMask;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.Supplier;

class Masks {

  private static final Map<Class<? extends Mask>, Supplier<? extends Mask>> EMPTY = new HashMap<>();
  private static final Map<Class<? extends Mask>, IntFunction<? extends Mask>> FULL = new HashMap<>();
  private static final Map<Class<? extends Mask>, Mask> SINGLETONS = new HashMap<>();
  static {
    EMPTY.put(WordMask.class, WordMask::new);
    EMPTY.put(ContainerMask.class, ContainerMask::new);
    EMPTY.put(RoaringBitmapMask.class, RoaringBitmapMask::new);
    FULL.put(WordMask.class, WordMask::contiguous);
    FULL.put(ContainerMask.class, ContainerMask::contiguous);
    FULL.put(RoaringBitmapMask.class, RoaringBitmapMask::contiguous);
    EMPTY.forEach((type, constructor) -> SINGLETONS.put(type, constructor.get()));
  }

  public static <MaskType extends Mask> MaskType createFull(Class<MaskType> type, int limit) {
    return type.cast(FULL.get(type).apply(limit));
  }

  public static <MaskType extends Mask> MaskType singleton(Class<MaskType> type) {
    return type.cast(SINGLETONS.get(type));
  }
}
