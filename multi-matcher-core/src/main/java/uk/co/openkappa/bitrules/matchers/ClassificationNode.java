package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.Mask;

public interface ClassificationNode<Input, MaskType extends Mask<MaskType>> {

  MaskType match(Input input);

  default float averageSelectivity() {
    return 1;
  }
}
