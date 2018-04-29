package uk.co.openkappa.bitrules.config;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/**
 * The attribute registry contains the association between
 * attribute keys and value accessors. A rule may not refer
 * to an attribute unless it is registered here.
 * @param <Key> the type of key to store attributes against
 * @param <T> the type of input object which may be classified.
 */
public class Schema<Key, T> {

  private final Map<Key, Attribute<T>> rules;
  
  private Schema(Map<Key, Attribute<T>> rules) {
    this.rules = rules;
  }

  /**
   * Create a new schema
   * @param <Key> the type of key to store attributes against
   * @param <U> the type of objects that values can be extracted from
   * @return the new schema
   */
  public static <Key, U> Schema<Key, U> newInstance() {
    return new Schema<>(new HashMap<>());
  }

  /**
   * Create a new schema
   * @param enumType the type of the enum
   * @param <Key> the key type
   * @param <U> the input type
   * @return a new schema
   */
  public static <Key extends Enum<Key>, U> Schema<Key, U> newInstance(Class<Key> enumType) {
    return new Schema<>(new EnumMap<>(enumType));
  }

  /**
   * Registers a generic attribute with equality semantics only
   * @param key the key of the attribute (rules refer to this)
   * @param accessor extracts a value of type U from the classified object
   * @param <U> the type of the attribute value
   * @return an attribute registry containing the attribute
   */
  public <U> Schema<Key, T> withAttribute(Key key, Function<T, U> accessor) {
    rules.put(key, new GenericAttribute<>(accessor));
    return this;
  }

  /**
   * Registers a generic attribute with equality and order semantics
   * @param key the key of the attribute (rules refer to this)
   * @param accessor extracts a value of type U from the classified object
   * @param <U> the type of the attribute value
   * @return an attribute registry containing the attribute
   */
  public <U> Schema<Key, T> withAttribute(Key key, Function<T, U> accessor, Comparator<U> comparator) {
    rules.put(key, new ComparableAttribute<>(comparator, accessor));
    return this;
  }

  /**
   * Registers a double attribute with equality and order semantics
   * @param key the key of the attribute (rules refer to this)
   * @param accessor extracts a value of type U from the classified object
   * @return an attribute registry containing the attribute
   */
  public Schema<Key, T> withAttribute(Key key, ToDoubleFunction<T> accessor) {
    rules.put(key, new DoubleAttribute<>(accessor));
    return this;
  }

  /**
   * Registers an int attribute with equality and order semantics
   * @param key the key of the attribute (rules refer to this)
   * @param accessor extracts a value of type U from the classified object
   * @return an attribute registry containing the attribute
   */
  public Schema<Key, T> withAttribute(Key key, ToIntFunction<T> accessor) {
    rules.put(key, new IntAttribute<>(accessor));
    return this;
  }

  /**
   * Registers a long attribute with equality and order semantics
   * @param key the key of the attribute (rules refer to this)
   * @param accessor extracts a value of type U from the classified object
   * @return an attribute registry containing the attribute
   */
  public Schema<Key, T> withAttribute(Key key, ToLongFunction<T> accessor) {
    rules.put(key, new LongAttribute<>(accessor));
    return this;
  }

  /**
   * Get the attribute with the supplied key if it exists
   * @param key the key of the attribute
   * @return the attribute if registered, otherwise null
   */
  public Attribute<T> getAttribute(Key key) {
    Attribute<T> attribute = rules.get(key);
    if (null == attribute) {
      throw new RuleAttributeNotRegistered("No attribute " + key + " registered.");
    }
    return attribute;
  }

}
