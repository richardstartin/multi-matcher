package uk.co.openkappa.bitrules.structures;

import java.util.*;
import java.util.function.ToIntFunction;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.comparingInt;
import static java.util.Comparator.nullsLast;

public abstract class PerfectHashMap<T> implements Map<String, T> {

  private static class State {
    private static final int SEED = 0xdeadbeef;
    private static final Hasher HASHER = XXHash64::hash;
  }

  private final static class TinyPerfectHashMap<T> extends PerfectHashMap<T> {

    private final Object[] values;
    private final int[] seeds;
    private final byte[] positions;
    private final long[] hashes;

    TinyPerfectHashMap(Object[] values, int[] seeds, byte[] positions, long[] hashes) {
      this.values = values;
      this.seeds = seeds;
      this.positions = positions;
      this.hashes = hashes;
    }

    @Override
    public int size() {
      return values.length;
    }

    @Override
    public boolean containsKey(Object key) {
      if (key instanceof String) {
        return position((String) key) >= 0;
      }
      return false;
    }

    @Override
    public T get(Object key) {
      if (key instanceof String) {
        int position = position((String) key);
        return position < 0 ? null : (T) values[position];
      }
      return null;
    }

    @Override
    public Collection<T> values() {
      List<T> values = new ArrayList<>(this.values.length);
      for (Object value : this.values) {
        if (null != value) {
          values.add((T) value);
        }
      }
      return values;
    }

    private int position(String data) {
      byte[] bytes = data.getBytes(UTF_8);
      long hash = State.HASHER.hash(bytes, State.SEED);
      int pos = (int) (hash & (seeds.length - 1));
      int seed = seeds[pos];
      if (seed == -1) {
        return -1;
      }
      if (hash != hashes[pos]) {
        return -1;
      }
      if (seed < 0) {
        return positions[seed & (positions.length - 1)];
      }
      return positions[(int) (scramble(hash, seed) & (positions.length - 1))] & 0xFF;
    }
  }

  private final static class SmallPerfectHashMap<T> extends PerfectHashMap<T> {

    private final Object[] values;
    private final int[] seeds;
    private final short[] positions;
    private final long[] hashes;

    SmallPerfectHashMap(Object[] values, int[] seeds, short[] positions, long[] hashes) {
      this.values = values;
      this.seeds = seeds;
      this.positions = positions;
      this.hashes = hashes;
    }


    @Override
    public int size() {
      return values.length;
    }

    @Override
    public boolean containsKey(Object key) {
      if (key instanceof String) {
        return position((String) key) >= 0;
      }
      return false;
    }

    @Override
    public T get(Object key) {
      if (key instanceof String) {
        int position = position((String) key);
        return position < 0 ? null : (T) values[position];
      }
      return null;
    }

    @Override
    public Collection<T> values() {
      List<T> values = new ArrayList<>(this.values.length);
      for (Object value : this.values) {
        if (null != value) {
          values.add((T) value);
        }
      }
      return values;
    }

    private int position(String data) {
      byte[] bytes = data.getBytes(UTF_8);
      long hash = State.HASHER.hash(bytes, State.SEED);
      int pos = (int) (hash & (seeds.length - 1));
      int seed = seeds[pos];
      if (seed == -1) {
        return -1;
      }
      if (hash != hashes[pos]) {
        return -1;
      }
      if (seed < 0) {
        return positions[seed & (positions.length - 1)];
      }
      return positions[(int) (scramble(hash, seed) & (positions.length - 1))] & 0xFFFF;
    }
  }

  private final static class HugePerfectHashMap<T> extends PerfectHashMap<T> {

    private final Object[] values;
    private final int[] seeds;
    private final int[] positions;
    private final long[] hashes;

    HugePerfectHashMap(Object[] values, int[] seeds, int[] positions, long[] hashes) {
      this.values = values;
      this.seeds = seeds;
      this.positions = positions;
      this.hashes = hashes;
    }


    @Override
    public int size() {
      return values.length;
    }

    @Override
    public boolean containsKey(Object key) {
      if (key instanceof String) {
        return position((String) key) >= 0;
      }
      return false;
    }

    @Override
    public T get(Object key) {
      if (key instanceof String) {
        int position = position((String) key);
        return position < 0 ? null : (T) values[position];
      }
      return null;
    }

    @Override
    public Collection<T> values() {
      List<T> values = new ArrayList<>(this.values.length);
      for (Object value : this.values) {
        if (null != value) {
          values.add((T) value);
        }
      }
      return values;
    }

    private int position(String data) {
      byte[] bytes = data.getBytes(UTF_8);
      long hash = State.HASHER.hash(bytes, State.SEED);
      int pos = (int) (hash & (seeds.length - 1));
      int seed = seeds[pos];
      if (seed == -1) {
        return -1;
      }
      if (hash != hashes[pos]) {
        return -1;
      }
      if (seed < 0) {
        return positions[seed & (positions.length - 1)];
      }
      return positions[(int) (scramble(hash, seed) & (positions.length - 1))];
    }
  }

  public static <T> Map<String, T> wrap(Map<String, T> map) {
    List<String> keys = new ArrayList<>(map.keySet());
    Object[] values = new Object[map.size()];
    int seed = State.SEED;
    int size = Math.max(2, Integer.bitCount(keys.size()) == 1 ? keys.size() : (Integer.highestOneBit(keys.size()) << 1));
    List<Slot>[] collisions = new List[size];
    int[] positions = new int[size];
    Arrays.fill(positions, -1);
    int[] seeds = new int[size];
    Arrays.fill(seeds, -1);
    long[] hashes = new long[size];
    for (int i = 0; i < keys.size(); ++i) {
      String key = keys.get(i);
      values[i] = map.get(key);
      byte[] bytes = key.getBytes(UTF_8);
      long hash = State.HASHER.hash(bytes, seed);
      int position = (int) (hash & (size - 1));
      List<Slot> slots = collisions[position];
      if (null == slots) {
        slots = new ArrayList<>();
        collisions[position] = slots;
      }
      slots.add(new Slot(bytes, i, hash));
    }
    Arrays.sort(collisions, nullsLast(comparingInt((ToIntFunction<List<Slot>>) List::size).reversed()));
    BitSet claimed = new BitSet(size);
    int position = 0;
    while (position < size && null != collisions[position] && collisions[position].size() > 1) {
      List<Slot> slots = collisions[position];
      int[] candidates = new int[slots.size()];
      trial:
      {
        ++seed;
        int s = 0;
        for (Slot slot : slots) {
          long hash = scramble(slot.hash, seed);
          int claim = (int) (hash & (size - 1));
          if (!claimed.get(claim) && !contains(candidates, s, claim)) {
            candidates[s++] = claim;
            hashes[claim] = slot.hash;
          } else {
            break trial;
          }
        }
        assert s == slots.size();
        for (int i = 0; i < s; ++i) {
          assert !claimed.get(candidates[i]);
          claimed.set(candidates[i]);
          assert positions[candidates[i]] == -1;
          positions[candidates[i]] = slots.get(i).position;
        }
        assert !contains(seeds, seeds.length, seed);
        assert seed > State.SEED;
        seeds[(int) (slots.get(0).hash & (size - 1))] = seed;
        ++position;
      }
    }
    int last = 0;
    while (position < size && null != collisions[position]) {
      Slot slot = collisions[position].get(0);
      int claim = claimed.nextClearBit(last);
      positions[claim] = slot.position;
      hashes[claim] = slot.hash;
      seeds[(int) (slot.hash & (size - 1))] = claim | 0x80000000;
      ++position;
      last = claim + 1;
    }
    if (size < 256) {
      assert positions[size - 1] < 256;
      byte[] minimised = new byte[size];
      for (int i = 0; i < minimised.length; ++i) {
        minimised[i] = (byte) positions[i];
      }
      return new TinyPerfectHashMap<>(values, seeds, minimised, hashes);
    } else if (size < 0x10000) {
      assert positions[size - 1] < 0x10000;
      short[] minimised = new short[size];
      for (int i = 0; i < minimised.length; ++i) {
        minimised[i] = (short) positions[i];
      }
      return new SmallPerfectHashMap<>(values, seeds, minimised, hashes);
    } else {
      return new HugePerfectHashMap<>(values, seeds, positions, hashes);
    }
  }


  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public T put(String key, T value) {
    throw new IllegalStateException("immutable");
  }

  @Override
  public T remove(Object key) {
    throw new IllegalStateException("immutable");
  }

  @Override
  public void putAll(Map<? extends String, ? extends T> m) {
    throw new IllegalStateException("immutable");
  }

  @Override
  public void clear() {
    throw new IllegalStateException("immutable");
  }

  @Override
  public Set<String> keySet() {
    throw new IllegalStateException("not iterable");
  }

  @Override
  public Set<Entry<String, T>> entrySet() {
    throw new IllegalStateException("not iterable");
  }

  @Override
  public boolean containsValue(Object value) {
    throw new IllegalStateException("wrong headed operation");
  }

  @FunctionalInterface
  private interface Hasher {
    long hash(byte[] data, int from, int to, int seed);

    default long hash(byte[] data, int seed) {
      return hash(data, 0, data.length, seed);
    }
  }

  private static boolean contains(int[] data, int limit, int value) {
    for (int i = 0; i < data.length && i < limit; ++i) {
      if (value == data[i]) {
        return true;
      }
    }
    return false;
  }

  private static long scramble(long value, int seed) {
    long x = value + seed;
    x ^= x >>> 12;
    x ^= x << 25;
    x ^= x >>> 27;
    return x * 2685821657736338717L;
  }

  private final static class Slot {
    private final byte[] serialised;
    private final int position;
    private final long hash;

    Slot(byte[] serialised, int position, long hash) {
      this.serialised = serialised;
      this.position = position;
      this.hash = hash;
    }

    @Override
    public String toString() {
      return new String(serialised);
    }


  }
}
