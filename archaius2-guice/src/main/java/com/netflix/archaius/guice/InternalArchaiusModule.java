package com.netflix.archaius.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.netflix.archaius.DefaultConfiguration;
import com.netflix.config.api.Configuration;
import com.netflix.config.api.Layers;
import com.netflix.config.api.MutablePropertySource;
import com.netflix.config.api.PropertyResolver;
import com.netflix.config.sources.PropertySources;
import com.netflix.config.sources.SynchronizedMutablePropertySource;
import com.netflix.governator.providers.AdvisableAnnotatedMethodScanner;
import com.netflix.governator.providers.Advises;
import com.netflix.governator.providers.ProvidesWithAdvice;

import java.util.function.UnaryOperator;

import javax.inject.Named;

/**
 * Base module to set up Archaius inside Guice.  Archaius setup is split into several bindings
 * that can be modified using @Advises LayeredPropertySource.  
 * 
 */
final class InternalArchaiusModule extends AbstractModule {
    
    @Override
    protected void configure() {
        install(AdvisableAnnotatedMethodScanner.asModule());
        install(ConfigurationInjectingListener.asModule());
    }
    
    @Provides
    @Singleton
    PropertyResolver getPropertySource(Configuration configuration) {
        return configuration.getPropertyResolver();
    }
    
    @Provides
    @Singleton
    Configuration getDefaultConfiguration(DefaultConfiguration.Builder builder) {
        return builder.build();
    }
    
    @ProvidesWithAdvice
    @Singleton
    DefaultConfiguration.Builder getDefaultConfigurationBuilder() {
        return DefaultConfiguration.builder();
    }
    
    @Provides
    @Singleton
    @Named("override")
    MutablePropertySource getMutablePropertySource() {
        return new SynchronizedMutablePropertySource("override");
    }
    
    @Advises(order = 0)
    @Singleton
    UnaryOperator<DefaultConfiguration> defaultAdvice(@Named("override") MutablePropertySource override) {
        return config -> {
            config.getPropertySource().addPropertySource(Layers.ENVIRONMENT, PropertySources.environment());
            config.getPropertySource().addPropertySource(Layers.SYSTEM, PropertySources.system());
            config.getPropertySource().addPropertySource(Layers.OVERRIDE, override);
            return config;
        };
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && getClass().equals(obj.getClass());
    }
}
