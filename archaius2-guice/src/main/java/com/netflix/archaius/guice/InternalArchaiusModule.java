package com.netflix.archaius.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.netflix.archaius.PropertySourceConfiguration;
import com.netflix.config.api.Configuration;
import com.netflix.config.api.Layers;
import com.netflix.config.api.PropertySource;
import com.netflix.config.sources.EnvironmentPropertySource;
import com.netflix.config.sources.LayeredPropertySource;
import com.netflix.config.sources.SystemPropertySource;
import com.netflix.governator.providers.AdvisableAnnotatedMethodScanner;
import com.netflix.governator.providers.ProvidesWithAdvice;

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
    PropertySource getPropertySource(LayeredPropertySource source) {
        return source;
    }
    
    @ProvidesWithAdvice
    @Singleton
    LayeredPropertySource getLayeredPropertySource() {
        LayeredPropertySource source = new LayeredPropertySource("root");
        source.addPropertySourceAtLayer(Layers.ENVIRONMENT, EnvironmentPropertySource.INSTANCE);
        source.addPropertySourceAtLayer(Layers.SYSTEM, SystemPropertySource.INSTANCE);
        return source;
    }
    
    @Provides
    @Singleton
    Configuration getConfiguration(PropertySource propertySource) {
        return new PropertySourceConfiguration(propertySource);
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
