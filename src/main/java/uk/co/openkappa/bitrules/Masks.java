package uk.co.openkappa.bitrules;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Masks {

  private static final Map<Class<? extends Mask>, Supplier<? extends Mask>> EMPTY = new HashMap<>();
  private static final Map<Class<? extends Mask>, Supplier<? extends Mask>> FULL = new HashMap<>();
  private static final Map<Class<? extends Mask>, Mask> SINGLETONS = new HashMap<>();
  static {
    EMPTY.put(TinyMask.class, TinyMask::new);
    EMPTY.put(ContainerMask.class, ContainerMask::new);
    FULL.put(TinyMask.class, TinyMask::full);
    FULL.put(ContainerMask.class, ContainerMask::full);
    EMPTY.forEach((type, constructor) -> SINGLETONS.put(type, constructor.get()));
  }

  public static <MaskType extends Mask> MaskType create(Class<MaskType> type) {
    return type.cast(EMPTY.get(type).get());
  }

  public static <MaskType extends Mask> MaskType createFull(Class<MaskType> type) {
    return type.cast(FULL.get(type).get());
  }

  public static <MaskType extends Mask> MaskType singleton(Class<MaskType> type) {
    return type.cast(SINGLETONS.get(type));
  }
}
