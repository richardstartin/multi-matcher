package io.github.richardstartin.multimatcher.core.masks;

import io.github.richardstartin.multimatcher.core.Mask;

public interface MaskFactory<MaskType extends Mask<MaskType>> {
  /**
   * Create an empty mask
   * @return an empty mask
   */
  MaskType empty();

  /**
   * Create a contiguous mask starting at zero
   * @param max the exclusive upper bound of the contiguous set
   * @return a contiguous mask with <code>max</code> bits set.
   */
  MaskType contiguous(int max);

  /**
   * Create a mask from the specified values
   * @param values the values to include
   * @return a mask from the values
   */
  MaskType of(int... values);

  /**
   * Create an empty mask which must not be modified
   * @return an empty mask
   */
  MaskType emptySingleton();

  /**
   * Create a contiguous mask starting at zero, which may need less allocation.
   * @param max the exclusive upper bound of the contiguous set
   * @return a contiguous mask with <code>max</code> bits set.
   */
  default MaskType memoryStableContiguousMask(int max) {
    return contiguous(max);
  }
}
