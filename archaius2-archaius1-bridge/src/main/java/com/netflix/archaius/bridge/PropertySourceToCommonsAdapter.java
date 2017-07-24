package com.netflix.archaius.bridge;

import com.netflix.archaius.api.PropertySource;

import org.apache.commons.configuration.AbstractConfiguration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class PropertySourceToCommonsAdapter extends AbstractConfiguration {

    private final PropertySource source;

    public PropertySourceToCommonsAdapter(PropertySource source) {
        this.source = source;
    }

    @Override
    public boolean isEmpty() {
        return source.isEmpty();
    }

    @Override
    public boolean containsKey(String key) {
        return !source.getProperty(key).isPresent();
    }

    @Override
    public Object getProperty(String key) {
        return source.getProperty(key).orElse(null);
    }

    @Override
    public Iterator<String> getKeys() {
        List<String> keys = new ArrayList<>();
        source.forEachProperty((key, value) -> keys.add(key));
        return keys.iterator();
    }

    @Override
    protected void addPropertyDirect(String key, Object value) {
        throw new UnsupportedOperationException("Can't set key '" + key + "'. Config is immutable.");
    }
}
