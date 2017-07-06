package com.netflix.archaius.api;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

/**
 * User facing contract for reading individual properties
 */
public interface PropertyResolver {

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

    /**
     * Get a property that has been interpolated and resolved to a specific type
     * @param key
     * @param type
     * @return
     */
    <T> Optional<T> getProperty(String key, Class<T> type);
}
