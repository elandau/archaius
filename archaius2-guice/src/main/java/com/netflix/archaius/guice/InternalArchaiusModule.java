package com.netflix.archaius.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.netflix.archaius.DefaultConfiguration;
import com.netflix.config.api.Configuration;
import com.netflix.config.api.SortedCompositePropertySource;
import com.netflix.config.api.Layers;
import com.netflix.config.api.PropertyResolver;
import com.netflix.config.api.PropertySource;
import com.netflix.config.sources.DefaultSortedCompositePropertySource;
import com.netflix.config.sources.EnvironmentPropertySource;
import com.netflix.config.sources.SystemPropertySource;
import com.netflix.governator.providers.AdvisableAnnotatedMethodScanner;
import com.netflix.governator.providers.Advises;
import com.netflix.governator.providers.ProvidesWithAdvice;

import java.util.function.UnaryOperator;

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
    
    @ProvidesWithAdvice
    @Singleton
    SortedCompositePropertySource getLayeredPropertySource() {
        return new DefaultSortedCompositePropertySource("root");
    }
    
    @Provides
    @Singleton
    PropertySource getPropertySource(SortedCompositePropertySource source) {
        return source;
    }
    
    @Provides
    @Singleton
    DefaultConfiguration getDefaultConfiguration(SortedCompositePropertySource propertySource) {
        return new DefaultConfiguration(propertySource);
    }
    
    @Provides
    @Singleton
    Configuration<? extends PropertySource> getConfiguration(DefaultConfiguration configuration) {
        return configuration; 
    }
    
    @Provides
    @Singleton
    PropertyResolver getPropertyResolver(DefaultConfiguration configuration) {
        return configuration;
    }

    @Advises(order = 0)
    @Singleton
    UnaryOperator<DefaultConfiguration> defaultAdvice() {
        return config -> {
            config.getPropertySource().addPropertySource(Layers.ENVIRONMENT, EnvironmentPropertySource.INSTANCE);
            config.getPropertySource().addPropertySource(Layers.SYSTEM, SystemPropertySource.INSTANCE);
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
