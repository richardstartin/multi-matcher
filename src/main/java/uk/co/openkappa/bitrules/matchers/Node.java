package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.Mask;

interface Node<Input, MaskType extends Mask<MaskType>> {
  MaskType match(Input input, MaskType context);
  void optimise();
}
