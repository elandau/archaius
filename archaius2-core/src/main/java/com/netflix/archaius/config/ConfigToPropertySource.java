package com.netflix.archaius.config;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.netflix.archaius.api.Config;
import com.netflix.config.api.PropertySource;

public class ConfigToPropertySource implements PropertySource {
    private final String name;
    private final Config config;
    
    public ConfigToPropertySource(String name, Config config) {
        this.name = name;
        this.config = config;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<Object> getProperty(String key) {
        return Optional.ofNullable(config.getRawProperty(key));
    }

    @Override
    public Collection<String> getKeys() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getKeys(String prefix) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return config.isEmpty();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEach(BiConsumer<String, Object> consumer) {
        config.forEach(consumer);
    }

    @Override
    public PropertySource snapshot() {
        return this;
    }

    @Override
    public void forEach(String prefix, BiConsumer<String, Object> consumer) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public PropertySource subset(String prefix) {
        // TODO Auto-generated method stub
        return null;
    }

}
