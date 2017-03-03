package com.netflix.config.api;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Optional;

/**
 * Top level configuration API for users to read properties. 
 */
public interface Configuration {

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
     * Return a 'raw' property that is neither interpolated nor resolved to a specific type
     * 
     * @param key
     * @return
     */
    public Optional<Object> getProperty(String key);
    
    /**
     * Get a property that has been interpolated and resolved to a specific type
     * @param key
     * @param type
     * @return
     */
    public <T> Optional<T> get(String key, Type type);
    
    /**
     * Get a property that has been interpolated and resolved to a specific type
     * @param key
     * @param type
     * @return
     */
    public <T> Optional<T> get(String key, Class<T> type);
    
    /**
     * Collection of all property names
     * @return
     */
    Collection<String> getPropertyNames();
    
    /**
     * @return True if there are no properties in the Configuration
     */
    boolean isEmpty();
}
