package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.Mask;

interface MutableNode<Input, MaskType extends Mask<MaskType>> extends ClassificationNode<Input, MaskType> {
  ClassificationNode<Input, MaskType> optimise();
}
