package com.netflix.archaius.sources;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.netflix.archaius.api.Cancellation;
import com.netflix.archaius.api.PropertySource;

public class EmptyPropertySource implements PropertySource {

    public static EmptyPropertySource INSTANCE = new EmptyPropertySource();
    
    private EmptyPropertySource() {
        
    }
    
    @Override
    public String getName() {
        return null;
    }

    @Override
    public Optional<Object> getProperty(String name) {
        return Optional.empty();
    }

    @Override
    public void forEach(BiConsumer<String, Object> consumer) {
    }

    @Override
    public void forEach(String prefix, BiConsumer<String, Object> consumer) {
    }

    @Override
    public Collection<String> getPropertyNames() {
        return Collections.emptySet();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Cancellation addListener(Consumer<PropertySource> consumer) {
        return () -> {};
    }
}
