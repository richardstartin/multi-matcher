package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.Mask;

public interface ClassificationNode<Input, MaskType extends Mask<MaskType>> {

    int match(Input input);

    default double averageSelectivity() {
        return 1;
    }
}
