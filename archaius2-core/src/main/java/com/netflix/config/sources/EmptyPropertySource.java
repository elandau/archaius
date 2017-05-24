package com.netflix.config.sources;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.netflix.config.api.PropertySource;

public class EmptyPropertySource implements PropertySource {

    public static EmptyPropertySource INSTANCE = new EmptyPropertySource();
    
    private EmptyPropertySource() {
        
    }
    
    @Override
    public String getName() {
        return "empty";
    }

    @Override
    public Optional<Object> getProperty(String name) {
        return Optional.empty();
    }

    @Override
    public Collection<String> getKeys() {
        return Collections.emptySet();
    }

    @Override
    public Collection<String> getKeys(String prefix) {
        return Collections.emptySet();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public int size() {
        return 0;
    }
    
    @Override
    public AutoCloseable addListener(Consumer<PropertySource> consumer) {
        return () -> {};
    }

    @Override
    public PropertySource snapshot() {
        return this;
    }

    @Override
    public void forEach(BiConsumer<String, Object> consumer) {
    }

    @Override
    public void forEach(String prefix, BiConsumer<String, Object> consumer) {
    }
    
    @Override
    public PropertySource subset(String prefix) {
        return this;
    }
}
