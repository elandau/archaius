package com.netflix.archaius.guice;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.function.BiConsumer;

import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.config.CompositeConfig;
import com.netflix.archaius.api.exceptions.ConfigException;
import com.netflix.archaius.config.AbstractConfig;
import com.netflix.archaius.config.ConfigToPropertySource;
import com.netflix.config.api.Layers;
import com.netflix.config.sources.LayeredPropertySource;

class LegacyLibraryLayerCompositeConfig extends AbstractConfig implements CompositeConfig {

    private final LayeredPropertySource propertySource;

    public LegacyLibraryLayerCompositeConfig(LayeredPropertySource propertySource) {
        this.propertySource = propertySource;
    }
    
    @Override
    public Object getRawProperty(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsKey(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<String> getKeys() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addConfig(String name, Config child) throws ConfigException {
        propertySource.addPropertySourceAtLayer(Layers.LIBRARIES, new ConfigToPropertySource(name, child));
        return false;
    }

    @Override
    public void replaceConfig(String name, Config child) throws ConfigException {
        throw new IllegalStateException();
    }

    @Override
    public void addConfigs(LinkedHashMap<String, Config> configs) throws ConfigException {
        configs.forEach((name, config) -> addConfig(name, config));
    }

    @Override
    public void replaceConfigs(LinkedHashMap<String, Config> configs) throws ConfigException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Config removeConfig(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Config getConfig(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getConfigNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEach(BiConsumer<String, Object> consumer) {
        propertySource.forEach(consumer);
    }
}
