package com.netflix.archaius.guice;

import java.util.Optional;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Provider;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.OptionalBinder;
import com.netflix.archaius.ConfigManager;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.archaius.DefaultDecoder;
import com.netflix.archaius.DefaultPropertyFactory;
import com.netflix.archaius.Layers;
import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.ConfigReader;
import com.netflix.archaius.api.Decoder;
import com.netflix.archaius.api.PropertyFactory;
import com.netflix.archaius.api.config.SettableConfig;
import com.netflix.archaius.api.inject.DefaultLayer;
import com.netflix.archaius.api.inject.RemoteLayer;
import com.netflix.archaius.api.inject.RuntimeLayer;
import com.netflix.archaius.config.DefaultSettableConfig;
import com.netflix.archaius.config.EnvironmentConfig;
import com.netflix.archaius.config.SystemConfig;
import com.netflix.archaius.readers.PropertiesConfigReader;

final class InternalArchaiusModule extends AbstractModule {
    static final String CONFIG_NAME_KEY         = "archaius.config.name";
    
    private static final String DEFAULT_CONFIG_NAME     = "application";
    
    @Override
    protected void configure() {
        ConfigurationInjectingListener listener = new ConfigurationInjectingListener();
        requestInjection(listener);
        bind(ConfigurationInjectingListener.class).toInstance(listener);
        requestStaticInjection(ConfigurationInjectingListener.class);
        bindListener(Matchers.any(), listener);
        
        Multibinder.newSetBinder(binder(), ConfigReader.class)
            .addBinding().to(PropertiesConfigReader.class).in(Scopes.SINGLETON);
        
        OptionalBinder.newOptionalBinder(binder(), ConfigManager.class);
    }

    @Provides
    @Singleton
    @RuntimeLayer
    SettableConfig getSettableConfig() {
        return new DefaultSettableConfig();
    }
    
    @Singleton
    private static class ConfigParameters {
        @Inject(optional=true)
        @Named(CONFIG_NAME_KEY)
        String configName;
        
        @Inject
        @RuntimeLayer
        SettableConfig  runtimeLayer;
        
        @Inject(optional=true)
        @RemoteLayer 
        Provider<Config> remoteLayerProvider;
        
        @Inject(optional=true)
        @DefaultLayer 
        Set<Config> defaultConfigs;
        
        @Inject(optional=true)
        @ApplicationOverride
        Config applicationOverride;

        @Inject(optional =true)
        @ApplicationOverrideResources
        Set<String> overrideResources;
        
        boolean hasApplicationOverride() {
            return applicationOverride != null;
        }
        
        boolean hasDefaultConfigs() {
            return defaultConfigs != null;
        }

        boolean hasRemoteLayer() {
            return remoteLayerProvider != null;
        }

        boolean hasOverrideResources() {
            return overrideResources != null;
        }
        
        String getConfigName() {
            return configName == null ? DEFAULT_CONFIG_NAME : configName;
        }
    }

    @Provides
    @Singleton
    @Raw
    ConfigManager getConfigManager(@Raw Optional<ConfigManager> externalConfigManager) {
        return externalConfigManager.orElseGet(() -> {
            ConfigManager configManager = new ConfigManager();
            configManager.addPropertySource(Layers.SYSTEM, SystemConfig.INSTANCE);
            configManager.addPropertySource(Layers.ENVIRONMENT, EnvironmentConfig.INSTANCE);
            return configManager;
        });
    }
    
    @Provides
    @Singleton
    @Raw
    Config getRawConfig(ConfigManager configManager) throws Exception {
        return configManager.getConfig();
    }

    @Provides
    @Singleton
    @RuntimeLayer
    SettableConfig getRuntimeLayer(ConfigManager configManager) {
        // TODO:
        return null;
    }
    
    @Provides
    @Singleton
    Config getConfig(ConfigParameters params, ConfigManager configManager, @Raw Optional<ConfigManager> externalConfigManager) throws Exception {
        if (externalConfigManager.isPresent()) {
            
        } else {
            // Load defaults layer
            if (params.hasDefaultConfigs()) {
                params.defaultConfigs.forEach(c -> configManager.addPropertySource(Layers.DEFAULT,  c));
            }
    
            if (params.hasOverrideResources()) {
                params.overrideResources.forEach(name -> 
                    configManager.addPropertySourceFromNamedResource(Layers.APPLICATION_OVERRIDE, name));
            }
    
            if (params.hasApplicationOverride()) {
                configManager.addPropertySource(Layers.APPLICATION_OVERRIDE, params.applicationOverride);
            }
            
            configManager.addPropertySourceFromNamedResource(Layers.APPLICATION, params.getConfigName());
    
            // Load remote properties
            if (params.hasRemoteLayer()) {
                configManager.addPropertySource(Layers.REMOTE, params.remoteLayerProvider.get());
            }
        }
        
        return configManager.getConfig();
    }
    
    @Provides
    @Singleton
    Decoder getDecoder() {
        return DefaultDecoder.INSTANCE;
    }

    @Provides
    @Singleton
    PropertyFactory getPropertyFactory(Config config) {
        return DefaultPropertyFactory.from(config);
    }

    @Provides
    @Singleton
    ConfigProxyFactory getProxyFactory(Config config, Decoder decoder, PropertyFactory factory) {
        return new ConfigProxyFactory(config, decoder, factory);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return InternalArchaiusModule.class.equals(obj.getClass());
    }
}
