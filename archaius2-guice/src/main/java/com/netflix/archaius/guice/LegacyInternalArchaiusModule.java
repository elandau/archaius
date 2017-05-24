package com.netflix.archaius.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.archaius.DefaultConfiguration;
import com.netflix.archaius.DefaultDecoder;
import com.netflix.archaius.DefaultPropertyFactory;
import com.netflix.archaius.api.CascadeStrategy;
import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.Decoder;
import com.netflix.archaius.api.PropertyFactory;
import com.netflix.archaius.api.config.SettableConfig;
import com.netflix.archaius.api.inject.DefaultLayer;
import com.netflix.archaius.api.inject.RemoteLayer;
import com.netflix.archaius.api.inject.RuntimeLayer;
import com.netflix.archaius.config.ConfigToPropertySource;
import com.netflix.archaius.config.ConfigurationToConfigAdapter;
import com.netflix.archaius.config.DefaultSettableConfig;
import com.netflix.config.api.PropertySourceSpec;
import com.netflix.config.api.Configuration;
import com.netflix.config.api.Layers;
import com.netflix.governator.providers.Advises;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

import javax.inject.Named;
import javax.inject.Provider;

public final class LegacyInternalArchaiusModule extends AbstractModule {
    public static final String CONFIG_NAME_KEY = "archaius.config.name";
    
    public static final int LEGACY_ADVICE_ORDER = 10;
    
    private final static AtomicInteger uniqueNameCounter = new AtomicInteger();

    private static String getUniqueName(String prefix) {
        return prefix +"-" + uniqueNameCounter.incrementAndGet();
    }
    
    @Override
    protected void configure() {
    }

    @Singleton
    private static class ConfigParameters {
        @Inject(optional=true)
        @Named(CONFIG_NAME_KEY)
        private String configName;
        
        @Inject(optional=true)
        CascadeStrategy       cascadingStrategy;
        
        @Inject(optional=true)
        @RemoteLayer 
        private Provider<Config> remoteLayerProvider;
        
        @Inject(optional=true)
        @DefaultLayer 
        private Set<Config> defaultConfigs;
        
        @Inject(optional=true)
        @ApplicationOverride
        private Provider<Config> applicationOverride;

        @Inject(optional =true)
        @ApplicationOverrideResources
        private Set<String> overrideResources;
        
        Set<Config> getDefaultConfigs() {
            return defaultConfigs != null ? defaultConfigs : Collections.emptySet();
        }

        Optional<Provider<Config>> getRemoteLayer() {
            return Optional.ofNullable(remoteLayerProvider);
        }

        Set<String> getOverrideResources() {
            return overrideResources != null ? overrideResources : Collections.emptySet();
        }
        
        Optional<String> getConfigName() {
            return Optional.ofNullable(configName);
        }
        
        Optional<CascadeStrategy> getCascadeStrategy() {
            return Optional.ofNullable(cascadingStrategy);
        }
        
        Optional<Provider<Config>> getApplicationOverride() {
            return Optional.ofNullable(applicationOverride);
        }
    }

    @Advises(order = LEGACY_ADVICE_ORDER)
    @Singleton
    UnaryOperator<DefaultConfiguration.Builder> adviseOptionalOverrideLayer(ConfigParameters params) throws Exception {
        return builder -> builder.configure(defaultConfiguration -> {
            params.getDefaultConfigs()
                .forEach(config -> defaultConfiguration.addPropertySource(
                        Layers.DEFAULTS, 
                        new ConfigToPropertySource(getUniqueName("default"), config)));
            
            params.getOverrideResources()
                .forEach(resourceName -> defaultConfiguration.addPropertySourceSpec(Layers.OVERRIDE, PropertySourceSpec.create(resourceName)));
            
            String applicationName = params.getConfigName().orElse("application");
            
            defaultConfiguration.addPropertySourceSpec(Layers.APPLICATION, PropertySourceSpec.create(applicationName));
            
            params.getApplicationOverride()
                .ifPresent(provider -> defaultConfiguration.addPropertySource(
                        Layers.APPLICATION_OVERRIDE, 
                        new ConfigToPropertySource(getUniqueName("override"), provider.get())));
            
//            params.getRemoteLayer()
//                .ifPresent(provider -> source.addPropertySource(Layers.REMOTE_OVERRIDE, (config) ->
//                        config.addConfigToLayer(Layers.REMOTE_OVERRIDE, "", provider.get())));
        });
    }

//    @Provides
//    @Singleton
//    @Raw
//    @Deprecated
//    Config getRawConfig(@Raw DefaultConfigManager configManager) {
//        return configManager;
//    }
    
    @Provides
    @Singleton
    @Deprecated
    Config getConfiguration(Configuration configuration) {
        return new ConfigurationToConfigAdapter(configuration);
    }
    
    @Provides
    @Singleton
    @RuntimeLayer
    @Deprecated
    SettableConfig getSettableConfig() {
        return new DefaultSettableConfig();
    }
    
    @Provides
    @Singleton
    @Deprecated
    PropertyFactory getPropertyFactory(Config config) {
        return DefaultPropertyFactory.from(config);
    }

    @Provides
    @Singleton
    @Deprecated
    ConfigProxyFactory getProxyFactory(Config config, Decoder decoder, PropertyFactory factory) {
        return new ConfigProxyFactory(config, decoder, factory);
    }
    
    @Provides
    @Singleton
    @Deprecated
    Decoder getDecoder() {
        return DefaultDecoder.INSTANCE;
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return getClass().equals(obj.getClass());
    }

}
