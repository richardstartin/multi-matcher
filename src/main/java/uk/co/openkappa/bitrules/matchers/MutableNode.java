package uk.co.openkappa.bitrules.matchers;

import uk.co.openkappa.bitrules.Mask;
import uk.co.openkappa.bitrules.Operation;

import java.util.Map;

public interface MutableNode<Input, MaskType extends Mask<MaskType>> extends ClassificationNode<Input, MaskType> {
  ClassificationNode<Input, MaskType> freeze();

  // TODO - better handled by vistor
  default void link(Map<Operation, MutableNode<Input, MaskType>> nodes) { }
}
