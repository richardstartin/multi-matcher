package uk.co.openkappa.bitrules.masks;

import uk.co.openkappa.bitrules.Mask;

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
}
