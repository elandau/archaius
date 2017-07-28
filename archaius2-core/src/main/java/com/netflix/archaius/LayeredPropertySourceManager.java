package com.netflix.archaius;

import com.netflix.archaius.api.CascadeStrategy;
import com.netflix.archaius.api.ConfigLoader.Loader;
import com.netflix.archaius.api.Layer;
import com.netflix.archaius.api.LayeredPropertySource;
import com.netflix.archaius.api.PropertyResolver;
import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.StringConverter;
import com.netflix.archaius.api.config.SettableConfig;
import com.netflix.archaius.cascade.NoCascadeStrategy;
import com.netflix.archaius.config.DefaultSettableConfig;
import com.netflix.archaius.readers.ServiceLoaderConfigReaderRegistry;
import com.netflix.archaius.source.DefaultLayeredPropertySource;
import com.netflix.archaius.source.InterpolatingPropertySource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class LayeredPropertySourceManager {
    private final CascadeStrategy defaultStrategy;
    private final InterpolatingPropertySource<LayeredPropertySource> propertySource;
    private final Map<Layer, SettableConfig> overrideLayers = new HashMap<>();
    private final ConfigReaderRegistry configReaderRegistry;
    private final Function<String, String> iterpolator;
    private final ResolvingPropertySource resolvingPropertySource;
    
    public static class Builder {
        private CascadeStrategy cascadeStrategy = new NoCascadeStrategy();
        private ConfigReaderRegistry readerRegistry;
        private LayeredPropertySource layeredPropertySource;
        private StringConverter.Registry stringConverterRegistry;
        
        public Builder withCascadeStrategy(CascadeStrategy cascadeStrategy) {
            this.cascadeStrategy = cascadeStrategy;
            return this;
        }
        
        public Builder withStringConverterRegistry(StringConverter.Registry registry) {
            this.stringConverterRegistry = registry;
            return this;
        }
        
        public LayeredPropertySourceManager build() {
            if (readerRegistry == null) {
                readerRegistry = new ServiceLoaderConfigReaderRegistry();
            }
            
            if (stringConverterRegistry == null) {
                stringConverterRegistry = new StringConverterRegistryBuilder().build();
            }
            
            layeredPropertySource = new DefaultLayeredPropertySource("root");
            return new LayeredPropertySourceManager(this);
        }
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }

    private LayeredPropertySourceManager(Builder builder) {
        this.propertySource = new InterpolatingPropertySource<LayeredPropertySource>(builder.layeredPropertySource);
        this.resolvingPropertySource = new ResolvingPropertySource(this.propertySource, builder.stringConverterRegistry);
        this.defaultStrategy = builder.cascadeStrategy;
        this.configReaderRegistry = builder.readerRegistry;
        this.iterpolator = propertySource::resolve;
    }
     
    public LayeredPropertySource getLayeredPropertySource() {
        return propertySource.delegate();
    }
    
    public ResolvingPropertySource getResolvingPropertySource() {
        return resolvingPropertySource;
    }
    
    public String resolve(String resolve) {
        return iterpolator.apply(resolve);
    }

    /**
     * Add a property source using a factory that takes the current state of the ConfigManager as input
     * and returns the PropertySource.
     * 
     * @param layer
     * @param loader
     */
    public synchronized void addPropertySource(Layer layer, Function<PropertyResolver, Optional<PropertySource>> loader) {
        loader
            .apply(resolvingPropertySource)
            .ifPresent(propertySource -> getLayeredPropertySource().addPropertySource(layer, propertySource));
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
        getLayeredPropertySource()
            .addPropertySource(layer, loader.apply(DefaultConfigLoader.builder()
                .withConfigReaders(this.configReaderRegistry.getReaders())
                .withDefaultCascadingStrategy(defaultStrategy)
                .withStrLookup(str -> (String)iterpolator.apply(str))
                .build()
                .newLoader()));
    }
    
    /**
     * Set a property override at a layer
     * @param layer
     * @param key
     * @param override
     */
    public synchronized void setPropertyOverride(Layer layer, String key, Object override) {
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
     */
    public synchronized void clearPropertyOverride(Layer layer, String key) {
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

    private synchronized SettableConfig addLayerOverride(Layer layer) {
        SettableConfig config = new DefaultSettableConfig(layer.getName() + "-overrides");
        getLayeredPropertySource().addPropertySource(layer, config, 0);
        return config;
    }
}
