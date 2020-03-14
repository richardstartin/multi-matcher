package io.github.richardstartin.multimatcher.core.matchers;

import io.github.richardstartin.multimatcher.core.Mask;
import io.github.richardstartin.multimatcher.core.Operation;

import java.util.Map;

public interface MutableNode<Input, MaskType extends Mask<MaskType>> {
    ClassificationNode<Input, MaskType> freeze();

    // TODO - better handled by vistor
    default void link(Map<Operation, MutableNode<Input, MaskType>> nodes) {
    }
}
