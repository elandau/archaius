package com.netflix.config.resolver;

import com.netflix.config.api.PropertyResolver;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;

abstract class AbstractDefaultPropertyResolver implements PropertyResolver {

    @Override
    final public Optional<Long> getLong(String key) {
        return getProperty(key, Long.class);
    }

    @Override
    final public Optional<String> getString(String key) {
        return getProperty(key, String.class);
    }

    @Override
    final public Optional<Double> getDouble(String key) {
        return getProperty(key, Double.class);
    }

    @Override
    final public Optional<Integer> getInteger(String key) {
        return getProperty(key, Integer.class);
    }

    @Override
    final public Optional<Boolean> getBoolean(String key) {
        return getProperty(key, Boolean.class);
    }

    @Override
    final public Optional<Short> getShort(String key) {
        return getProperty(key, Short.class);
    }

    @Override
    final public Optional<BigInteger> getBigInteger(String key) {
        return getProperty(key, BigInteger.class);
    }

    @Override
    final public Optional<BigDecimal> getBigDecimal(String key) {
        return getProperty(key, BigDecimal.class);
    }

    @Override
    final public Optional<Float> getFloat(String key) {
        return getProperty(key, Float.class);
    }

    @Override
    final public Optional<Byte> getByte(String key) {
        return getProperty(key, Byte.class);
    }

    @Override
    final public <K, V> Optional<Map<K, V>> getMap(String key, Class<K> keyType, Class<V> valueType) {
        return getObject(key, ParameterizedTypes.mapOf(keyType, valueType));
    }
}
