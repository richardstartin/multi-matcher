package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.Mask;

public interface ClassificationNode<Input, MaskType extends Mask<MaskType>> {

  MaskType match(Input input);

  default float averageSelectivity() {
    return 1;
  }
}
