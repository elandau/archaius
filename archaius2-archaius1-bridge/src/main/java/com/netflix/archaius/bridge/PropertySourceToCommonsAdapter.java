package com.netflix.archaius.bridge;

import java.util.Iterator;

import org.apache.commons.configuration.AbstractConfiguration;

import com.netflix.config.api.PropertySource;

/**
 * Adapter from an Archaius2 configuration to an Apache Commons Configuration.
 * 
 * Note that since Archaius2 treats the Config as immutable setting properties
 * is not allowed.
 * 
 * @author elandau
 */
class PropertySourceToCommonsAdapter extends AbstractConfiguration {

    private PropertySource propetySource;

    public PropertySourceToCommonsAdapter(PropertySource propetySource) {
        this.propetySource = propetySource;
    }
    
    @Override
    public boolean isEmpty() {
        return propetySource.isEmpty();
    }

    @Override
    public boolean containsKey(String key) {
        return propetySource.getProperty(key).isPresent();
    }

    @Override
    public Object getProperty(String key) {
        return propetySource.getProperty(key).orElse(null);
    }

    @Override
    public Iterator<String> getKeys() {
        return propetySource.getKeys().iterator();
    }

    @Override
    protected void addPropertyDirect(String key, Object value) {
        throw new UnsupportedOperationException("Can't set key '" + key + "'. Config is immutable.");
    }
}
