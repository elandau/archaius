package com.netflix.archaius.bridge;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.Configuration;

import com.netflix.archaius.api.config.SettableConfig;
import com.netflix.archaius.api.exceptions.ConfigException;
import com.netflix.archaius.api.inject.RuntimeLayer;
import com.netflix.archaius.commons.CommonsToPropertySource;
import com.netflix.archaius.exceptions.ConfigAlreadyExistsException;
import com.netflix.config.AggregatedConfiguration;
import com.netflix.config.DeploymentContext;
import com.netflix.config.DynamicPropertySupport;
import com.netflix.config.PropertyListener;
import com.netflix.config.api.Layers;
import com.netflix.config.api.PropertySource;
import com.netflix.config.sources.LayeredPropertySource;

/**
 * @see StaticArchaiusBridgeModule
 */
@Singleton
class AbstractConfigurationBridge extends AbstractConfiguration implements AggregatedConfiguration, DynamicPropertySupport {

    private final LayeredPropertySource propertySource;
    private final SettableConfig settable;
    private final AtomicInteger libNameCounter = new AtomicInteger();
    
    {
        AbstractConfiguration.setDefaultListDelimiter('\0');
    }
    
    @Inject
    public AbstractConfigurationBridge(
            final LayeredPropertySource propertySource,
            @RuntimeLayer SettableConfig settable, 
            DeploymentContext context) {
        this.propertySource = propertySource;
        this.settable = settable;
    }
    
    @Override
    public boolean isEmpty() {
        return propertySource.isEmpty();
    }

    @Override
    public boolean containsKey(String key) {
        return propertySource.getProperty(key).isPresent();
    }
    
    @Override
    public String getString(String key, String defaultValue) {
        return propertySource.getProperty(key).map(value -> value.toString()).orElse(defaultValue);
    }

    @Override
    public Object getProperty(String key) {
        return propertySource.getProperty(key).orElse(null);  // Should interpolate
    }

    @Override
    public Iterator<String> getKeys() {
        return propertySource.getKeys().iterator();
    }

    @Override
    protected void addPropertyDirect(String key, Object value) {
        settable.setProperty(key, value);
    }

    @Override
    public void addConfiguration(AbstractConfiguration config) {
        addConfiguration(config, "Config-" + libNameCounter.incrementAndGet());
    }

    @Override
    public void addConfiguration(AbstractConfiguration config, String name) {
        try {
            this.propertySource.addPropertySourceAtLayer(Layers.LIBRARIES, new CommonsToPropertySource(name, config));
        }
        catch (ConfigAlreadyExistsException e) {
            // OK To ignore
        } 
        catch (ConfigException e) {
            throw new RuntimeException("Unable to add configuration " + name, e);
        }
    }

    @Override
    public Set<String> getConfigurationNames() {
        return propertySource.flattened().map(PropertySource::getName).collect(Collectors.toSet());
    }

    @Override
    public List<String> getConfigurationNameList() {
        return propertySource.flattened().map(PropertySource::getName).collect(Collectors.toList());
    }

    @Override
    public Configuration getConfiguration(String name) {
        throw new UnsupportedOperationException(name);
    }

    @Override
    public int getNumberOfConfigurations() {
        return getConfigurationNames().size();
    }

    @Override
    public Configuration getConfiguration(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<AbstractConfiguration> getConfigurations() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration removeConfiguration(String name) {
        throw new UnsupportedOperationException(name);
    }

    @Override
    public boolean removeConfiguration(Configuration config) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration removeConfigurationAt(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addConfigurationListener(final PropertyListener expandedPropertyListener) {
        propertySource.addListener(source -> expandedPropertyListener.configSourceLoaded(this));
    }
}
