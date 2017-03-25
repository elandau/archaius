package com.netflix.config.api;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;

/**
 * Main API for users to read properties
 * 
 * This API knows nothing about the structure of the backing configuration, which could
 * be a single PropertySource or a composite and dynamic property source
 */
public interface PropertyResolver {

    public Optional<Long> getLong(String key);

    public Optional<String> getString(String key);

    public Optional<Double> getDouble(String key);

    public Optional<Integer> getInteger(String key);

    public Optional<Boolean> getBoolean(String key);

    public Optional<Short> getShort(String key);

    public Optional<BigInteger> getBigInteger(String key);

    public Optional<BigDecimal> getBigDecimal(String key);

    public Optional<Float> getFloat(String key);

    public Optional<Byte> getByte(String key);

    /**
     * Get a property that has been interpolated and resolved to a specific type
     * @param key
     * @param type
     * @return
     */
    public <T> Optional<T> getProperty(String key, Type type);
    
    /**
     * Get a property that has been interpolated and resolved to a specific type
     * @param key
     * @param type
     * @return
     */
    public <T> Optional<T> getProperty(String key, Class<T> type);
    
    /**
     * Return of an instance of T with properties mapped to this instance.  If T
     * is an interface a Proxy will be created for it.
     * 
     * @param prefix
     * @param type
     * @return
     */
    public <T> Optional<T> getObject(String prefix, Class<T> type);
    
    public <T> Optional<T> getObject(String prefix, Type type);
    
    /**
     * Return an immutable map of keyType to valueType
     * 
     * @param keyType
     * @param valueType
     * @return
     */
    public <K, V> Optional<Map<K, V>> getMap(String prefix, Class<K> keyType, Class<V> valueType);
    
    /**
     * Return a resolver containing only properties with the specified prefix
     * @param prefix
     * @return
     */
    public PropertyResolver withPrefix(String prefix);
    
    /**
     * Return a resolver that will fallback to properties with the specified prefix.
     * Note that 'prefix' will be appended to any prefix of this PropertyResolver
     * 
     * @param prefix 
     * @return
     */
//    public PropertyResolver withFallback(String prefix);
    
    /**
     * Return a resolver that will fallback to the specified resolver if a property
     * is not found.
     * 
     * @param resolver
     * @return
     */
//    public PropertyResolver withFallback(PropertyResolver resolver);
}
