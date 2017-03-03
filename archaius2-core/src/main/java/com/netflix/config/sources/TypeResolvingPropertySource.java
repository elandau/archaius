package com.netflix.config.sources;

import com.netflix.config.api.Configuration;
import com.netflix.config.api.PropertySource;
import com.netflix.config.api.TypeResolver;
import com.netflix.config.resolver.ResolverLookupImpl;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

public class TypeResolvingPropertySource extends DelegatingPropertySource implements Configuration {

    private final TypeResolver.Registry registry;
    private final PropertySource delegate;
    
    public TypeResolvingPropertySource(PropertySource source) {
        this(source, new ResolverLookupImpl());
    }

    public TypeResolvingPropertySource(PropertySource source, TypeResolver.Registry registry) {
        this.delegate = new InterpolatingPropertySource(source);
        this.registry = registry;
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
    public <T> Optional<T> get(String key, Type type) {
        try {
            return (Optional<T>) Optional.ofNullable(registry.get(type).resolve(key, this, registry));
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to get property " + key + " of type " + type, e);
        }
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            return (Optional<T>) Optional.ofNullable(registry.get(type).resolve(key, this, registry));
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to get property " + key + " of type " + type, e);
        }
    }

    @Override
    public TypeResolvingPropertySource subset(String prefix) {
        if (prefix.isEmpty()) {
            return this;
        } else if (!prefix.endsWith(".")) {
            return subset(prefix + ".");
        } else {
            return new TypeResolvingPropertySource(delegate().subset(prefix), registry);
        }
    }

    @Override
    protected PropertySource delegate() {
        return delegate;
    }
}
