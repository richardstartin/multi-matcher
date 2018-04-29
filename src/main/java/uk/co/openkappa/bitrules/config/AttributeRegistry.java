package uk.co.openkappa.bitrules.config;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/**
 * The attribute registry contains the association between
 * attribute names and value accessors. A rule may not refer
 * to an attribute unless it is registered here.
 * @param <T> the type of input object which may be classified.
 */
public class AttributeRegistry<T> {

  private Map<String, Attribute<T>> rules = new HashMap<>();

  /**
   * Create a new attribute registry
   * @param <U> the type of objects that values can be extracted from
   * @return the new attribute registry
   */
  public static <U> AttributeRegistry<U> newInstance() {
    return new AttributeRegistry<>();
  }

  /**
   * Registers a generic attribute with equality semantics only
   * @param name the name of the attribute (rules refer to this)
   * @param accessor extracts a value of type U from the classified object
   * @param <U> the type of the attribute value
   * @return an attribute registry containing the attribute
   */
  public <U> AttributeRegistry<T> withAttribute(String name, Function<T, U> accessor) {
    rules.put(name, new GenericAttribute<>(accessor));
    return this;
  }

  /**
   * Registers a generic attribute with equality and order semantics
   * @param name the name of the attribute (rules refer to this)
   * @param accessor extracts a value of type U from the classified object
   * @param <U> the type of the attribute value
   * @return an attribute registry containing the attribute
   */
  public <U> AttributeRegistry<T> withAttribute(String name, Function<T, U> accessor, Comparator<U> comparator) {
    rules.put(name, new ComparableAttribute<>(comparator, accessor));
    return this;
  }

  /**
   * Registers a double attribute with equality and order semantics
   * @param name the name of the attribute (rules refer to this)
   * @param accessor extracts a value of type U from the classified object
   * @return an attribute registry containing the attribute
   */
  public AttributeRegistry<T> withAttribute(String name, ToDoubleFunction<T> accessor) {
    rules.put(name, new DoubleAttribute<>(accessor));
    return this;
  }

  /**
   * Registers an int attribute with equality and order semantics
   * @param name the name of the attribute (rules refer to this)
   * @param accessor extracts a value of type U from the classified object
   * @return an attribute registry containing the attribute
   */
  public AttributeRegistry<T> withAttribute(String name, ToIntFunction<T> accessor) {
    rules.put(name, new IntAttribute<>(accessor));
    return this;
  }

  /**
   * Registers a long attribute with equality and order semantics
   * @param name the name of the attribute (rules refer to this)
   * @param accessor extracts a value of type U from the classified object
   * @return an attribute registry containing the attribute
   */
  public AttributeRegistry<T> withAttribute(String name, ToLongFunction<T> accessor) {
    rules.put(name, new LongAttribute<>(accessor));
    return this;
  }

  /**
   * Checks if an attribute has been registered
   * @param name the name of the attribute
   * @return true if an attribute with the name has been registered
   */
  public boolean hasAttribute(String name) {
    return rules.containsKey(name);
  }

  /**
   * Get the attribute with the supplied name if it exists
   * @param name the name of the attribute
   * @return the attribute if registered, otherwise null
   */
  public Attribute<T> getAttribute(String name) {
    return rules.get(name);
  }

}
