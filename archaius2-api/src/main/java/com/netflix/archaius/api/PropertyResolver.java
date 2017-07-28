package com.netflix.archaius.api;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * User API for reading typed properties.
 */
public interface PropertyResolver {

    /**
     * Get a property as a specific type.  If the internal representation is a string the 
     * PropertyResolver will attempt to convert to the request type using a 
     * {@link StringConverter}.
     * 
     * @param key
     * @param type
     * @return
     */
    <T> Optional<T> getProperty(String key, Class<T> type);
    
    <T> Optional<T> getProperty(String key, Type type);
    
    default Optional<Long> getLong(String key) { return getProperty(key, Long.class); }

    default Optional<String> getString(String key) { return getProperty(key, String.class); }

    default Optional<Double> getDouble(String key) { return getProperty(key, Double.class); }

    default Optional<Integer> getInteger(String key) { return getProperty(key, Integer.class); }

    default Optional<Boolean> getBoolean(String key) { return getProperty(key, Boolean.class); }

    default Optional<Short> getShort(String key) { return getProperty(key, Short.class); }

    default Optional<BigInteger> getBigInteger(String key) { return getProperty(key, BigInteger.class); }

    default Optional<BigDecimal> getBigDecimal(String key) { return getProperty(key, BigDecimal.class); }

    default Optional<Float> getFloat(String key) { return getProperty(key, Float.class); }

    default Optional<Byte> getByte(String key) { return getProperty(key, Byte.class); }

    <T> Optional<List<T>> getList(String key, Class<T> type);
        
    /**
     * Resolve a value without the need to look up a property
     * @param value
     * @param type
     * @return
     */
    <T> T resolve(String value, Class<T> type);
    
    <T> T resolve(String value, Type type);
}
