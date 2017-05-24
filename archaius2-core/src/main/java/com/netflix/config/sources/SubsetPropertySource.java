package com.netflix.config.sources;

import com.netflix.config.api.PropertySource;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Delegating property source that is a subset of another PropertySource
 */
public final class SubsetPropertySource implements PropertySource {
    private final String path;
    private final PropertySource delegate;
    
    public SubsetPropertySource(PropertySource source, String path) {
        this.delegate = source;
        this.path = path;
    }
    
    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Optional<Object> getProperty(String key) {
        return delegate.getProperty(path + key);
    }

    @Override
    public Collection<String> getKeys() {
        return delegate.getKeys(path);
    }

    @Override
    public Collection<String> getKeys(String prefix) {
        if (prefix.isEmpty()) {
            return getKeys();
        }
        if (!prefix.endsWith(".")) {
            return getKeys(prefix + ".");
        }
        return delegate.getKeys(path + prefix);
    }

    @Override
    public boolean isEmpty() {
        return getKeys().isEmpty();
    }

    @Override
    public int size() {
        return getKeys().size();
    }

    @Override
    public void forEach(BiConsumer<String, Object> consumer) {
        delegate.forEach(path, consumer);
    }

    @Override
    public void forEach(String prefix, BiConsumer<String, Object> consumer) {
        if (prefix.isEmpty()) {
            forEach(consumer);
        } else if (!prefix.endsWith(".")) {
            forEach(prefix + ".", consumer);
        } else {
            delegate.forEach(path + prefix, consumer);
        }
    }
    
    @Override
    public PropertySource subset(String prefix) {
        if (prefix.isEmpty()) {
            return this;
        } else if (!prefix.endsWith(".")) {
            return subset(prefix + ".");
        } else {
            return new SubsetPropertySource(delegate, path + prefix);
        }
    }

    @Override
    public PropertySource snapshot() {
        return new SubsetPropertySource(delegate.snapshot(), path);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
