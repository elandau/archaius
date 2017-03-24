package com.netflix.archaius;

import com.netflix.config.api.Bundle;
import com.netflix.config.api.Configuration;
import com.netflix.config.api.PropertyResolver;
import com.netflix.config.api.PropertySource;
import com.netflix.config.api.SortedCompositePropertySource;
import com.netflix.config.api.SortedCompositePropertySource.Layer;
import com.netflix.config.api.TypeResolver;
import com.netflix.config.resolver.DefaultTypeResolverRegistry;
import com.netflix.config.sources.DefaultPropertyResolver;
import com.netflix.config.sources.DefaultSortedCompositePropertySource;
import com.netflix.config.sources.InterpolatingPropertySource;
import com.netflix.config.sources.formats.BundleToPropertySource;
import com.netflix.config.sources.formats.PropertySourceFactoryContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Main configuration API both to set up a configuration structure as well as to read properties.
 */
public class DefaultConfiguration implements Configuration {

    public static class Builder {
        private final List<Consumer<DefaultConfiguration>> consumers = new ArrayList<>();
        private PropertySourceFactoryContext propertySourceContext = PropertySourceFactoryContext.DEFAULT;
        
        public Builder configure(Consumer<DefaultConfiguration> consumer) {
            this.consumers.add(consumer);
            return this;
        }
        
        public DefaultConfiguration build() {
            DefaultConfiguration config = new DefaultConfiguration(
                    new DefaultSortedCompositePropertySource("root"),
                    propertySourceContext,
                    new DefaultTypeResolverRegistry()
                    );
            
            consumers.forEach(c -> c.accept(config));
            return config;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static DefaultConfiguration createDefault() {
        return builder().build();
    }
    
    private final SortedCompositePropertySource source;
    private final PropertySourceFactoryContext propertySourceContext;
    private final PropertyResolver resolver;
    
    private DefaultConfiguration(
            SortedCompositePropertySource source, 
            PropertySourceFactoryContext propertySourceContext,
            TypeResolver.Registry registry) {
        InterpolatingPropertySource i = new InterpolatingPropertySource(source);
        this.source = source;
        this.resolver = new DefaultPropertyResolver(i, registry);
        this.propertySourceContext = propertySourceContext.withInterpolator(i);
    }
    
    @Override
    public PropertyResolver getPropertyResolver() {
        return resolver;
    }
    
    @Override
    public void addPropertySource(Layer layer, PropertySource source) {
        this.source.addPropertySource(layer, source);
    }

    @Override
    public void addBundle(Layer layer, Bundle bundle) {
        addPropertySource(layer, new BundleToPropertySource().load(bundle, propertySourceContext));
    }

    @Override
    public SortedCompositePropertySource getPropertySource() {
        return source;
    }
}
