package com.netflix.archaius.bridge;

import com.netflix.archaius.ConfigManager;
import com.netflix.archaius.Layers;
import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.commons.CommonsToConfig;
import com.netflix.config.AggregatedConfiguration;
import com.netflix.config.DeploymentContext;
import com.netflix.config.DynamicPropertySupport;
import com.netflix.config.PropertyListener;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.Configuration;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @see StaticArchaiusBridgeModule
 * @author elandau
 */
@Singleton
class AbstractConfigurationBridge extends AbstractConfiguration implements AggregatedConfiguration, DynamicPropertySupport {

    private final AtomicInteger libNameCounter = new AtomicInteger();
    private final ConfigManager configManager;
    
    {
        AbstractConfiguration.setDefaultListDelimiter('\0');
    }
    
    @Inject
    public AbstractConfigurationBridge(
            final ConfigManager configManager,
            DeploymentContext context) {
        this.configManager = configManager;
    }
    
    @Override
    public boolean isEmpty() {
        return configManager.getConfig().isEmpty();
    }

    @Override
    public boolean containsKey(String key) {
        return configManager.getConfig().containsKey(key);
    }
    
    @Override
    public String getString(String key, String defaultValue) {
        return configManager.getConfig().getString(key, defaultValue);
    }

    @Override
    public Object getProperty(String key) {
        return configManager.getConfig().getRawProperty(key);  // Should interpolate
    }

    @Override
    public Iterator<String> getKeys() {
        return configManager.getConfig().getKeys();
    }

    @Override
    protected void addPropertyDirect(String key, Object value) {
        configManager.setPropertyOverride(Layers.RUNTIME, key, value);
    }

    @Override
    public void addConfiguration(AbstractConfiguration config) {
        addConfiguration(config, "Config-" + libNameCounter.incrementAndGet());
    }

    @Override
    public void addConfiguration(AbstractConfiguration config, String name) {
        configManager.addPropertySource(Layers.LIBRARY, new CommonsToConfig(config, name));
    }

    @Override
    public Set<String> getConfigurationNames() {
        return configManager
                .getLayeredPropertySource()
                .getPropertySourcesAtLayer(Layers.LIBRARY)
                .stream()
                .map(PropertySource::getName)
                .collect(Collectors.toSet());
    }

    @Override
    public List<String> getConfigurationNameList() {
        return configManager.getLayeredPropertySource()
                .getPropertySources()
                .stream()
                .map(source -> source.getName())
                .collect(Collectors.toList());
    }

    @Override
    public Configuration getConfiguration(String name) {
        for (PropertySource source : configManager.getLayeredPropertySource().getPropertySourcesAtLayer(Layers.LIBRARY)) {
            if (source.getName().equals(name)) {
                return new PropertySourceToCommonsAdapter(source);
            }
        }
        return null;
    }

    @Override
    public int getNumberOfConfigurations() {
        return configManager.getLayeredPropertySource().getPropertySourcesAtLayer(Layers.LIBRARY).size();
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
        configManager.getLayeredPropertySource().removePropertySource(Layers.LIBRARY, name);
        return null;    // TODO: Should we return the old source
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
        configManager.getLayeredPropertySource().addChangeEventListener(
                event -> expandedPropertyListener.configSourceLoaded(event.getPropertySource()));
    }
}
