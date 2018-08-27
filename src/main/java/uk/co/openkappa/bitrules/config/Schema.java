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
 * @param <InputType> the type of input object which may be classified.
 */
public class Schema<Key, InputType> {

  private final Map<Key, Attribute<InputType>> rules;
  
  private Schema(Map<Key, Attribute<InputType>> rules) {
    this.rules = rules;
  }

  /**
   * Create a new create
   * @param <Key> the type of key to store attributes against
   * @param <InputType> the type of objects that values can be extracted from
   * @return the new create
   */
  public static <Key, InputType> Schema<Key, InputType> create() {
    return new Schema<>(new HashMap<>());
  }

  /**
   * Create a new create
   * @param enumType the type of the enum
   * @param <Key> the key type
   * @param <InputType> the input type
   * @return a new create
   */
  public static <Key extends Enum<Key>, InputType> Schema<Key, InputType> create(Class<Key> enumType) {
    return new Schema<>(new EnumMap<>(enumType));
  }

  /**
   * Registers a generic attribute builder equality semantics only
   * @param key the key of the attribute (rules refer to this)
   * @param accessor extracts a value of type InputType from the classified object
   * @param <U> the type of the attribute value
   * @return an attribute registry containing the attribute
   */
  public <U> Schema<Key, InputType> withAttribute(Key key, Function<InputType, U> accessor) {
    rules.put(key, new GenericAttribute<>(accessor));
    return this;
  }

  /**
   * Registers a generic attribute builder equality and order semantics
   * @param key the key of the attribute (rules refer to this)
   * @param accessor extracts a value of type InputType from the classified object
   * @param <U> the type of the attribute value
   * @return an attribute registry containing the attribute
   */
  public <U> Schema<Key, InputType> withAttribute(Key key, Function<InputType, U> accessor, Comparator<U> comparator) {
    rules.put(key, new ComparableAttribute<>(comparator, accessor));
    return this;
  }

  /**
   * Registers a double attribute builder equality and order semantics
   * @param key the key of the attribute (rules refer to this)
   * @param accessor extracts a value of type InputType from the classified object
   * @return an attribute registry containing the attribute
   */
  public Schema<Key, InputType> withAttribute(Key key, ToDoubleFunction<InputType> accessor) {
    rules.put(key, new DoubleAttribute<>(accessor));
    return this;
  }

  /**
   * Registers an int attribute builder equality and order semantics
   * @param key the key of the attribute (rules refer to this)
   * @param accessor extracts a value of type InputType from the classified object
   * @return an attribute registry containing the attribute
   */
  public Schema<Key, InputType> withAttribute(Key key, ToIntFunction<InputType> accessor) {
    rules.put(key, new IntAttribute<>(accessor));
    return this;
  }

  /**
   * Registers a long attribute builder equality and order semantics
   * @param key the key of the attribute (rules refer to this)
   * @param accessor extracts a value of type InputType from the classified object
   * @return an attribute registry containing the attribute
   */
  public Schema<Key, InputType> withAttribute(Key key, ToLongFunction<InputType> accessor) {
    rules.put(key, new LongAttribute<>(accessor));
    return this;
  }

  /**
   * Get the attribute builder the supplied key if it exists
   * @param key the key of the attribute
   * @return the attribute if registered, otherwise null
   */
  public Attribute<InputType> getAttribute(Key key) {
    Attribute<InputType> attribute = rules.get(key);
    if (null == attribute) {
      throw new AttributeNotRegistered("No attribute " + key + " registered.");
    }
    return attribute;
  }

}
