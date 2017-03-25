package com.netflix.config.resolver;

import com.netflix.config.api.PropertyResolver;
import com.netflix.config.api.PropertySource;
import com.netflix.config.api.TypeResolver;

import java.lang.reflect.Type;
import java.util.Optional;

/**
 * PropertySource capable of resolving property value to any Java type support by a {@link TypeResolver.Registery}
 */
public class DefaultPropertyResolver extends AbstractDefaultPropertyResolver {

    private final TypeResolver.Registry registry;
    private final PropertySource source;
    
    public DefaultPropertyResolver(PropertySource source, TypeResolver.Registry registry) {
        this.source = source;
        this.registry = registry;
    }

    @Override
    public <T> Optional<T> getProperty(String key, Type type) {
        try {
            TypeResolver<T> resolver = registry.get(type);
            return source.getProperty(key).map(value -> resolver.resolve(value, registry));
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to get property " + key + " of type " + type, e);
        }
    }

    @Override
    public <T> Optional<T> getProperty(String key, Class<T> type) {
        try {
            TypeResolver<T> resolver = registry.get(type);
            return source.getProperty(key).map(value -> resolver.resolve(value, registry));
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to get property " + key + " of type " + type, e);
        }
    }

    @Override
    public <T> Optional<T> getObject(String prefix, Class<T> type) {
        try {
            return (Optional<T>) Optional
                    .ofNullable(registry.get(type).resolve(prefix, source, registry));
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to get property " + prefix + " of type " + type, e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> getObject(String prefix, Type type) {
        try {
            return (Optional<T>) Optional.ofNullable(registry.get(type).resolve(prefix, source, registry));
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to get property " + prefix + " of type " + type, e);
        }
    }

    @Override
    public PropertyResolver withPrefix(String prefix) {
        return new DefaultPropertyResolver(source.subset(prefix), registry);
    }
}
