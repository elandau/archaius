package com.netflix.archaius;

import com.netflix.config.api.Configuration;
import com.netflix.config.api.ConfigurationNode;
import com.netflix.config.api.PropertySource;
import com.netflix.config.api.TypeResolver.Registry;
import com.netflix.config.resolver.ResolverLookupImpl;
import com.netflix.config.sources.InterpolatingPropertySource;
import com.netflix.config.sources.PropertySourcePropertyNode;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Optional;

public class PropertySourceConfiguration implements Configuration {

    private final Registry lookup;
    private final PropertySource source;
    private final PropertySource interpolated;
    
    public PropertySourceConfiguration(PropertySource source) {
        this.interpolated = new InterpolatingPropertySource(source);
        this.source = source;
        this.lookup = new ResolverLookupImpl();
    }

    @Override
    public Optional<Long> getLong(String key) {
        return get(key, Long.class);
    }

    @Override
    public Optional<String> getString(String key) {
        return get(key, String.class);
    }

    @Override
    public Optional<Double> getDouble(String key) {
        return get(key, Double.class);
    }

    @Override
    public Optional<Integer> getInteger(String key) {
        return get(key, Integer.class);
    }

    @Override
    public Optional<Boolean> getBoolean(String key) {
        return get(key, Boolean.class);
    }

    @Override
    public Optional<Short> getShort(String key) {
        return get(key, Short.class);
    }

    @Override
    public Optional<BigInteger> getBigInteger(String key) {
        return get(key, BigInteger.class);
    }

    @Override
    public Optional<BigDecimal> getBigDecimal(String key) {
        return get(key, BigDecimal.class);
    }

    @Override
    public Optional<Float> getFloat(String key) {
        return get(key, Float.class);
    }

    @Override
    public Optional<Byte> getByte(String key) {
        return get(key, Byte.class);
    }

    @Override
    public Optional<Object> getProperty(String key) {
        return source.getProperty(key);
    }
    
    @Override
    public <T> Optional<T> get(String key, Type type) {
        try {
            ConfigurationNode node = new PropertySourcePropertyNode(interpolated.snapshot()).getChild(key);
            return (Optional<T>) Optional.ofNullable(lookup.get(type).resolve(node, lookup));
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to get property " + key + " of type " + type, e);
        }
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            ConfigurationNode node = new PropertySourcePropertyNode(interpolated.snapshot()).getChild(key);
            return Optional.ofNullable(lookup.forType(type).resolve(node, lookup));
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to get property " + key + " of type " + type, e);
        }
    }

    @Override
    public boolean isEmpty() {
        return source.isEmpty();
    }

    @Override
    public Collection<String> getPropertyNames() {
        return source.getKeys();
    }
}
