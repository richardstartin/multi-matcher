package uk.co.openkappa.bitrules.config;

/**
 * Thrown if a rule is specified on an attribute not found in the attribute registry.
 */
public class RuleAttributeNotRegistered extends RuntimeException {

  public RuleAttributeNotRegistered(String message) {
    super(message);
  }
}
