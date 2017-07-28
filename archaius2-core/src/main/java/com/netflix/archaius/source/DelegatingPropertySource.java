package com.netflix.archaius.source;

import com.netflix.archaius.api.PropertySource;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class DelegatingPropertySource implements PropertySource {

    @Override
    public void addChangeEventListener(Consumer<ChangeEvent> consumer) {
        delegate().addChangeEventListener(consumer);
    }

    @Override
    public boolean isEmpty() {
        return delegate().isEmpty();
    }

    @Override
    public Iterable<String> getPropertyNames() {
        return delegate().getPropertyNames();
    }

    @Override
    public  Optional<Object> getProperty(String key) { 
        return delegate().getProperty(key); 
    }

    @Override
    public  void forEachProperty(BiConsumer<String, Object> consumer) {
        delegate().forEachProperty(consumer);
    }

    @Override
    public String getName() { 
        return delegate().getName(); 
    }
    
    public abstract PropertySource delegate();
}
