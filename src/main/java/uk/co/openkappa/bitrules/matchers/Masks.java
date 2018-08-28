package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.masks.SmallMask;
import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.masks.HugeMask;
import uk.co.openkappa.bitrules.masks.TinyMask;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.Supplier;

class Masks {

  private static final Map<Class<? extends Mask>, Supplier<? extends Mask>> EMPTY = new HashMap<>();
  private static final Map<Class<? extends Mask>, IntFunction<? extends Mask>> FULL = new HashMap<>();
  private static final Map<Class<? extends Mask>, Mask> SINGLETONS = new HashMap<>();
  static {
    EMPTY.put(TinyMask.class, TinyMask::new);
    EMPTY.put(SmallMask.class, SmallMask::new);
    EMPTY.put(HugeMask.class, HugeMask::new);
    FULL.put(TinyMask.class, TinyMask::contiguous);
    FULL.put(SmallMask.class, SmallMask::contiguous);
    FULL.put(HugeMask.class, HugeMask::contiguous);
    EMPTY.forEach((type, constructor) -> SINGLETONS.put(type, constructor.get()));
  }

  public static <MaskType extends Mask> MaskType wildcards(Class<MaskType> type, int limit) {
    return type.cast(FULL.get(type).apply(limit));
  }

  public static <MaskType extends Mask> MaskType singleton(Class<MaskType> type) {
    return type.cast(SINGLETONS.get(type));
  }
}
