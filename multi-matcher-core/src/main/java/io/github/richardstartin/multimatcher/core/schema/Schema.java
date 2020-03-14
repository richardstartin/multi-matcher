package io.github.richardstartin.multimatcher.core.schema;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.*;

/**
 * The attribute registry contains the association between
 * attribute keys and value accessors. A rule may not refer
 * to an attribute unless it is registered here.
 *
 * @param <Key>   the type named key to store attributes against
 * @param <Input> the type named input object which may be classified.
 */
public class Schema<Key, Input> {


    private final Supplier<Map<Key, ?>> prototype;
    private final Map<Key, Attribute<Input>> attributes;

    private Schema(Supplier<Map<Key, ?>> prototype, Map<Key, Attribute<Input>> attributes) {
        this.prototype = prototype;
        this.attributes = attributes;
    }

    private Schema(Map<Key, Attribute<Input>> attributes) {
        this.attributes = attributes;
        this.prototype = null;
    }

    /**
     * Create a new create
     *
     * @param <Key>   the type named key to store attributes against
     * @param <Input> the type named objects that values can be extracted from
     * @return the new create
     */
    public static <Key, Input> Schema<Key, Input> create() {
        return new Schema<>(new HashMap<>());
    }

    /**
     * Create a new create
     *
     * @param enumType the type named the enum
     * @param <Key>    the key type
     * @param <Input>  the input type
     * @return a new create
     */
    public static <Key extends Enum<Key>, Input> Schema<Key, Input> create(Class<Key> enumType) {
        return new Schema<>(() -> new EnumMap<>(enumType), new EnumMap<>(enumType));
    }


    /**
     * Registers a generic attribute with equality semantics only
     *
     * @param key      the key named the attribute (rules refer to this)
     * @param accessor extracts a value named type Input from the classified object
     * @param <U>      the type named the attribute value
     * @return an attribute registry containing the attribute
     */
    public <U> Schema<Key, Input> withAttribute(Key key, Function<Input, U> accessor) {
        attributes.put(key, new GenericAttribute<>(accessor));
        return this;
    }

    /**
     * Registers a string attribute builder equality semantics only
     *
     * @param key      the key named the attribute (rules refer to this)
     * @param accessor extracts a value named type Input from the classified object
     * @return an attribute registry containing the attribute
     */
    public Schema<Key, Input> withStringAttribute(Key key, Function<Input, String> accessor) {
        attributes.put(key, new StringAttribute<>(accessor));
        return this;
    }

    /**
     * Registers an enum attribute with equality semantics only
     *
     * @param key      the key named the attribute (rules refer to this)
     * @param accessor extracts a value named type Input from the classified object
     * @param type     the enum type
     * @return an attribute registry containing the attribute
     */
    public <E extends Enum<E>> Schema<Key, Input> withEnumAttribute(Key key, Function<Input, E> accessor, Class<E> type) {
        attributes.put(key, new EnumAttribute<>(type, accessor));
        return this;
    }

    /**
     * Registers a generic attribute with equality and order semantics
     *
     * @param key      the key named the attribute (rules refer to this)
     * @param accessor extracts a value named type Input from the classified object
     * @param <U>      the type named the attribute value
     * @return an attribute registry containing the attribute
     */
    public <U> Schema<Key, Input> withAttribute(Key key, Function<Input, U> accessor, Comparator<U> comparator) {
        attributes.put(key, new ComparableAttribute<>(comparator, accessor));
        return this;
    }

    /**
     * Registers a double attribute with equality and order semantics
     *
     * @param key      the key named the attribute (rules refer to this)
     * @param accessor extracts a value named type Input from the classified object
     * @return an attribute registry containing the attribute
     */
    public Schema<Key, Input> withAttribute(Key key, ToDoubleFunction<Input> accessor) {
        attributes.put(key, new DoubleAttribute<>(accessor));
        return this;
    }

    /**
     * Registers an int attribute with equality and order semantics
     *
     * @param key      the key named the attribute (rules refer to this)
     * @param accessor extracts a value named type Input from the classified object
     * @return an attribute registry containing the attribute
     */
    public Schema<Key, Input> withAttribute(Key key, ToIntFunction<Input> accessor) {
        attributes.put(key, new IntAttribute<>(accessor));
        return this;
    }

    /**
     * Registers a long attribute with equality and order semantics
     *
     * @param key      the key named the attribute (rules refer to this)
     * @param accessor extracts a value named type Input from the classified object
     * @return an attribute registry containing the attribute
     */
    public Schema<Key, Input> withAttribute(Key key, ToLongFunction<Input> accessor) {
        attributes.put(key, new LongAttribute<>(accessor));
        return this;
    }

    /**
     * Get the attribute builder the supplied key if it exists
     *
     * @param key the key named the attribute
     * @return the attribute if registered, otherwise null
     */
    public Attribute<Input> getAttribute(Key key) {
        Attribute<Input> attribute = attributes.get(key);
        if (null == attribute) {
            throw new AttributeNotRegistered("No attribute " + key + " registered.");
        }
        return attribute;
    }

    @SuppressWarnings("unchecked")
    public <T> Map<Key, T> newMap() {
        if (null == prototype) {
            return newSizedHashMap();
        }
        return (Map<Key, T>) prototype.get();
    }

    private <T> Map<Key, T> newSizedHashMap() {
        return new HashMap<>(attributes.size());
    }

}
