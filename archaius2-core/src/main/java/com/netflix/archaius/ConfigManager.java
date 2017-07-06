package com.netflix.archaius;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.netflix.archaius.api.CascadeStrategy;
import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.ConfigLoader.Loader;
import com.netflix.archaius.api.ConfigMapper;
import com.netflix.archaius.api.ConfigReader;
import com.netflix.archaius.api.Layer;
import com.netflix.archaius.api.LayeredPropertySource;
import com.netflix.archaius.api.PropertyResolver;
import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.config.SettableConfig;
import com.netflix.archaius.cascade.NoCascadeStrategy;
import com.netflix.archaius.config.DefaultLayeredConfig;
import com.netflix.archaius.config.DefaultSettableConfig;
import com.netflix.archaius.internal.ConfigPropertyResolver;
import com.netflix.archaius.interpolate.ConfigStrLookup;

/**
 * Top level API to the entire config hierarchy using Layers as the top level structure 
 */
public class ConfigManager implements ConfigMapper {
    private final Set<ConfigReader> readers = new LinkedHashSet<>();
    private final Map<Layer, SettableConfig> overrideLayers = new HashMap<>();
    private final DefaultLayeredConfig layeredConfig;
    private final CascadeStrategy defaultStrategy;
    private final ConfigProxyFactory proxyFactory;
    private final PropertyResolver propertyResolver;
    
    /**
     * Construct a ConfigManager
     * @param defaultCascadeStrategy Cascade strategy to use for all resources that are being loaded
     */
    public ConfigManager(CascadeStrategy defaultCascadeStrategy) {
        this.defaultStrategy = defaultCascadeStrategy;
        this.layeredConfig = new DefaultLayeredConfig("manager");
        this.proxyFactory = new ConfigProxyFactory(layeredConfig);
        this.propertyResolver = new ConfigPropertyResolver(layeredConfig);
    }
    
    public ConfigManager() {
        this(new NoCascadeStrategy());
    }
    
    /**
     * Add a ConfigReader to use from this point forward.  Previously added resources will not be reloaded.
     * @param reader
     */
    public synchronized void addConfigReader(ConfigReader reader) {
        this.readers.add(reader);
    }
    
    /**
     * Add a property source into a layer
     * @param layer
     * @param propertySource
     */
    public synchronized void addPropertySource(Layer layer, PropertySource propertySource) {
        layeredConfig.addPropertySource(layer, propertySource);
    }

    /**
     * Add a property source using a factory that takes the current state of the ConfigManager as input
     * and returns the PropertySource.
     * 
     * @param layer
     * @param loader
     */
    public synchronized void addPropertySource(Layer layer, Function<PropertyResolver, Optional<PropertySource>> loader) {
        loader.apply(propertyResolver).ifPresent(propertySource -> layeredConfig.addPropertySource(layer, propertySource));
    }
    
    /**
     * Load a resource into a layer
     * @param layer
     * @param resourceName
     */
    public synchronized void addPropertySourceFromNamedResource(Layer layer, String resourceName) {
        loadResource(layer, loader -> {
            try {
                return loader.load(resourceName);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load resource " + resourceName + " into layer " + layer, e);
            }
        });
    }
    
    /**
     * Load a resource into a layer using the provided loader.  The loader gets as input the default ConfigLoader
     * using the configured CascadeStrategy and ConfigReaders but can be customized for things like a different
     * CascadeStrategy or ClassLoader.
     * @param layer
     * @param loader
     */
    public synchronized void loadResource(Layer layer, Function<Loader, PropertySource> loader) {
        addPropertySource(layer, loader.apply(DefaultConfigLoader.builder()
            .withConfigReaders(readers)
            .withDefaultCascadingStrategy(defaultStrategy)
            .withStrLookup(ConfigStrLookup.from(layeredConfig))
            .build()
            .newLoader()));
    }
    
    /**
     * Set a property override at a layer
     * @param layer
     * @param key
     * @param override
     */
    public void setPropertyOverride(Layer layer, String key, Object override) {
        setPropertyOverrides(layer, Collections.singletonMap(key, override));
    }
    
    /**
     * Bulk set overrides
     * @param layer
     * @param overrides
     */
    public synchronized void setPropertyOverrides(Layer layer, Map<String, Object> overrides) {
        overrideLayers
            .computeIfAbsent(layer, this::addLayerOverride)
            .setProperties(overrides);
    }

    /**
     * Clear a property override.  getProperty for the key will return the value from the winning PropertySource
     * already configured on the layer
     * @param layer
     * @param key
     * @param override
     * @return Chainable ConfigManager
     */
    public void clearPropertyOverride(Layer layer, String key) {
        clearPropertyOverrides(layer, Collections.singleton(key));
    }
    
    /**
     * Bulk clear property overrides.  getProperty on any of the keys will return the value from the winning
     * PropertSource already configured on the layer.
     * @param layer
     * @param keys
     */
    public synchronized void clearPropertyOverrides(Layer layer, Set<String> keys) {
        overrideLayers
            .computeIfAbsent(layer, this::addLayerOverride)
            .clearProperties(keys);
    }

    /**
     * @return Return the underlying config
     */
    public Config getConfig() {
        return layeredConfig;
    }
    
    public LayeredPropertySource getLayeredPropertySource() {
        return layeredConfig;
    }
    
    private SettableConfig addLayerOverride(Layer layer) {
        SettableConfig config = new DefaultSettableConfig(layer.getName() + "-overrides");
        layeredConfig.addPropertySource(layer, config, 0);
        return config;
    }

    @Override
    public <T> T createInstance(Class<T> type) {
        return proxyFactory.createInstance(type);
    }    
}
