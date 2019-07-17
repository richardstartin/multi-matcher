package uk.co.openkappa.bitrules.structures;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

class XXHash64 {

  private static final Unsafe UNSAFE;
  private static final long BYTE_ARRAY_OFFSET;

  static {
    try {
      Class unsafeClass = Class.forName("sun.misc.Unsafe");
      Field declaredField = unsafeClass.getDeclaredField("theUnsafe");
      declaredField.setAccessible(true);
      UNSAFE = (Unsafe) declaredField.get(null);
      BYTE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private static final long PRIME_32_1 = 0b10011110001101110111100110110001L;
  private static final long PRIME_32_2 = 0b10000101111010111100101001110111L;
  private static final long PRIME_32_3 = 0b11000010101100101010111000111101L;
  private static final long PRIME_32_4 = 0b00100111110101001110101100101111L;
  private static final long PRIME_32_5 = 0b00010110010101100110011110110001L;

  private XXHash64() {}


  public static long hash(byte[] input, int offset, int length, long seed) {
    long hash;
    long remaining = length;

    if (remaining >= 32) {
      long v1 = seed + PRIME_32_1 + PRIME_32_2;
      long v2 = seed + PRIME_32_2;
      long v3 = seed;
      long v4 = seed - PRIME_32_1;

      do {
        v1 += UNSAFE.getLong(input, BYTE_ARRAY_OFFSET + offset) * PRIME_32_2;
        v1 = Long.rotateLeft(v1, 31);
        v1 *= PRIME_32_1;

        v2 += UNSAFE.getLong(input, BYTE_ARRAY_OFFSET + offset + 8) * PRIME_32_2;
        v2 = Long.rotateLeft(v2, 31);
        v2 *= PRIME_32_1;

        v3 += UNSAFE.getLong(input, BYTE_ARRAY_OFFSET + offset + 16) * PRIME_32_2;
        v3 = Long.rotateLeft(v3, 31);
        v3 *= PRIME_32_1;

        v4 += UNSAFE.getLong(input, BYTE_ARRAY_OFFSET + offset + 24) * PRIME_32_2;
        v4 = Long.rotateLeft(v4, 31);
        v4 *= PRIME_32_1;

        offset += 32;
        remaining -= 32;
      } while (remaining >= 32);

      hash = Long.rotateLeft(v1, 1)
              + Long.rotateLeft(v2, 7)
              + Long.rotateLeft(v3, 12)
              + Long.rotateLeft(v4, 18);

      v1 *= PRIME_32_2;
      v1 = Long.rotateLeft(v1, 31);
      v1 *= PRIME_32_1;
      hash ^= v1;
      hash = hash * PRIME_32_1 + PRIME_32_4;

      v2 *= PRIME_32_2;
      v2 = Long.rotateLeft(v2, 31);
      v2 *= PRIME_32_1;
      hash ^= v2;
      hash = hash * PRIME_32_1 + PRIME_32_4;

      v3 *= PRIME_32_2;
      v3 = Long.rotateLeft(v3, 31);
      v3 *= PRIME_32_1;
      hash ^= v3;
      hash = hash * PRIME_32_1 + PRIME_32_4;

      v4 *= PRIME_32_2;
      v4 = Long.rotateLeft(v4, 31);
      v4 *= PRIME_32_1;
      hash ^= v4;
      hash = hash * PRIME_32_1 + PRIME_32_4;
    } else {
      hash = seed + PRIME_32_5;
    }

    hash += length;

    while (remaining >= 8) {
      long k1 = UNSAFE.getLong(input, BYTE_ARRAY_OFFSET + offset);
      k1 *= PRIME_32_2;
      k1 = Long.rotateLeft(k1, 31);
      k1 *= PRIME_32_1;
      hash ^= k1;
      hash = Long.rotateLeft(hash, 27) * PRIME_32_1 + PRIME_32_4;
      offset += 8;
      remaining -= 8;
    }

    if (remaining >= 4) {
      hash ^= UNSAFE.getInt(input, BYTE_ARRAY_OFFSET + offset) * PRIME_32_1;
      hash = Long.rotateLeft(hash, 23) * PRIME_32_2 + PRIME_32_3;
      offset += 4;
      remaining -= 4;
    }

    while (remaining != 0) {
      hash ^= input[offset] * PRIME_32_5;
      hash = Long.rotateLeft(hash, 11) * PRIME_32_1;
      --remaining;
      ++offset;
    }

    hash ^= hash >>> 33;
    hash *= PRIME_32_2;
    hash ^= hash >>> 29;
    hash *= PRIME_32_3;
    hash ^= hash >>> 32;
    return hash;
  }
}
