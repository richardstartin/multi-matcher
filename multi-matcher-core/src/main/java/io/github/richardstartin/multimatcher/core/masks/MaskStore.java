package io.github.richardstartin.multimatcher.core.masks;

import io.github.richardstartin.multimatcher.core.Mask;

public interface MaskStore<MaskType extends Mask<MaskType>> {
    /**
     * Create an empty mask
     *
     * @return an empty mask
     */
    MaskType newMask();

    int newMaskId();

    int newMaskId(int copyAddress);

    int storeMask(MaskType mask);

    MaskType getMask(int id);

    void add(int id, int bit);

    void remove(int id, int bit);

    void or(int from, int into);

    void andNot(int from, int into);

    void optimise(int id);

    MaskType getTemp();

    MaskType getTemp(int copyAddress);

    void orInto(MaskType mask, int id);

    void andInto(MaskType mask, int id);

    /**
     * Create a contiguous mask starting at zero
     *
     * @param max the exclusive upper bound of the contiguous set
     * @return a contiguous mask with <code>max</code> bits set.
     */
    MaskType contiguous(int max);

    int newContiguousMaskId(int max);


    boolean isEmpty(int id);

    /**
     * Create a mask from the specified values
     *
     * @param values the values to include
     * @return a mask from the values
     */
    MaskType of(int... values);

    double averageSelectivity(int[] masks, int min, int max);

    default double averageSelectivity(int[] masks) {
        return averageSelectivity(masks, 0, masks.length);
    }
}
