package com.netflix.config.sources;

import com.netflix.config.api.PropertySource;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class DelegatingPropertySource implements PropertySource {

    @Override
    public PropertySource snapshot() {
        return delegate().snapshot();
    }
    
    @Override
    public String getName() {
        return delegate().getName();
    }

    @Override
    public Optional<Object> getProperty(String name) {
        return delegate().getProperty(name);
    }

    @Override
    public Collection<String> getKeys() {
        return delegate().getKeys();
    }

    @Override
    public Collection<String> getKeys(String prefix) {
        return delegate().getKeys(prefix);
    }
    
    @Override
    public boolean isEmpty() {
        return delegate().isEmpty();
    }
    
    @Override
    public int size() {
        return delegate().size();
    }

    @Override
    public AutoCloseable addListener(Consumer<PropertySource> consumer) {
        return delegate().addListener(consumer);
    }

    @Override
    public void forEach(BiConsumer<String, Object> consumer) {
        delegate().forEach(consumer);
    }

    @Override
    public void forEach(String prefix, BiConsumer<String, Object> consumer) {
        delegate().forEach(prefix, consumer);
    }
    
    @Override
    public PropertySource subset(String prefix) {
        if (prefix.isEmpty()) {
            return this;
        } else if (!prefix.endsWith(".")) {
            return subset(prefix + ".");
        } else {
            return new SubsetPropertySource(this, prefix);
        }
    }

    protected abstract PropertySource delegate();
}
