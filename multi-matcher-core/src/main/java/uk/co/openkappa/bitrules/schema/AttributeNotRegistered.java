package uk.co.openkappa.bitrules.schema;

/**
 * Thrown if a rule is specified on an attribute not found in the attribute registry.
 */
public class AttributeNotRegistered extends RuntimeException {

  AttributeNotRegistered(String message) {
    super(message);
  }
}
