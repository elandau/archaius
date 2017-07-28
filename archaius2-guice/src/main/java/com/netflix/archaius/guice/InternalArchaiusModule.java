package com.netflix.archaius.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.OptionalBinder;
import com.google.inject.name.Names;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.archaius.DefaultPropertyFactory;
import com.netflix.archaius.LayeredPropertySourceManager;
import com.netflix.archaius.Layers;
import com.netflix.archaius.ResolvingPropertySource;
import com.netflix.archaius.StringConverterRegistryBuilder;
import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.ConfigReader;
import com.netflix.archaius.api.PropertyFactory;
import com.netflix.archaius.api.PropertyResolver;
import com.netflix.archaius.api.config.SettableConfig;
import com.netflix.archaius.api.inject.DefaultLayer;
import com.netflix.archaius.api.inject.RemoteLayer;
import com.netflix.archaius.api.inject.RuntimeLayer;
import com.netflix.archaius.config.PropertySourceToConfig;
import com.netflix.archaius.readers.PropertiesConfigReader;

import java.util.Set;

import javax.inject.Named;
import javax.inject.Provider;

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
        
        OptionalBinder.newOptionalBinder(binder(), ResolvingPropertySource.class);
    }

    @Singleton
    private static class ConfigParameters {
        @Inject(optional=true)
        @Named(CONFIG_NAME_KEY)
        String configName;
        
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
    @RuntimeLayer
    SettableConfig getRuntimeLayer(LayeredPropertySourceManager configManager) {
        // TODO:
        return null;
    }
    
    @Provides
    @Singleton
    Config getConfig(LayeredPropertySourceManager manager) throws Exception {
        return new PropertySourceToConfig(manager.getResolvingPropertySource());
    }
    
    @Provides
    @Singleton
    PropertyFactory getPropertyFactory(Config config) {
        return DefaultPropertyFactory.from(config);
    }

    @Provides
    @Singleton
    LayeredPropertySourceManager getPropertySourceManager(Injector injector, ConfigParameters params) {
        LayeredPropertySourceManager configManager = LayeredPropertySourceManager
                .newBuilder()
                .withStringConverterRegistry(new StringConverterRegistryBuilder()
                    .withNotFoundConverter(type -> {
                        System.out.println("Creating converter for type : " + type);
                        return value -> injector.getInstance(Key.get(type, Names.named(value)));
                    })
                    .build()
                )
                .build();
        
        // Load defaults layer
        if (params.hasDefaultConfigs()) {
            params.defaultConfigs.forEach(c -> configManager.getLayeredPropertySource().addPropertySource(Layers.DEFAULT,  c));
        }

        if (params.hasOverrideResources()) {
            params.overrideResources.forEach(name -> 
                configManager.addPropertySourceFromNamedResource(Layers.APPLICATION_OVERRIDE, name));
        }

        if (params.hasApplicationOverride()) {
            configManager.getLayeredPropertySource().addPropertySource(Layers.APPLICATION_OVERRIDE, params.applicationOverride);
        }
        
        configManager.addPropertySourceFromNamedResource(Layers.APPLICATION, params.getConfigName());

        // Load remote properties
        if (params.hasRemoteLayer()) {
            configManager.getLayeredPropertySource().addPropertySource(Layers.REMOTE, params.remoteLayerProvider.get());
        }

        return configManager;
    }
    
    @Provides
    @Singleton
    ConfigProxyFactory getProxyFactory(LayeredPropertySourceManager manager) {
        return new ConfigProxyFactory(manager.getResolvingPropertySource());
    }
    
    @Provides
    @Singleton
    PropertyResolver getPropertyResolver(LayeredPropertySourceManager manager) {
        return manager.getResolvingPropertySource();
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
