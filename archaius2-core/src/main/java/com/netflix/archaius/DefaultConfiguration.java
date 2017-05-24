package com.netflix.archaius;

import com.netflix.config.api.PropertySourceSpec;
import com.netflix.config.api.Configuration;
import com.netflix.config.api.Layer;
import com.netflix.config.api.PropertyResolver;
import com.netflix.config.api.PropertySource;
import com.netflix.config.api.SortedCompositePropertySource;
import com.netflix.config.api.TypeResolver;
import com.netflix.config.resolver.DefaultPropertyResolver;
import com.netflix.config.resolver.DefaultTypeResolverRegistry;
import com.netflix.config.sources.DefaultSortedCompositePropertySource;
import com.netflix.config.sources.ImmutablePropertySource;
import com.netflix.config.sources.InterpolatingPropertySource;
import com.netflix.config.sources.formats.PropertySourceFactoryContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main configuration API both to set up a configuration structure as well as to read properties.
 */
public class DefaultConfiguration implements Configuration {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultConfiguration.class);
    
    public static class Builder {
        private final List<Consumer<DefaultConfiguration>> consumers = new ArrayList<>();
        private final Map<Type, TypeResolver<?>> resolvers = new HashMap<>();
        private PropertySourceFactoryContext propertySourceContext = PropertySourceFactoryContext.DEFAULT;
        
        public <T> Builder registerTypeResolver(Class<T> type, TypeResolver<T> resolver) {
            resolvers.put(type, resolver);
            return this;
        }
        
        public <T> Builder registerTypeResolver(Type type, TypeResolver<T> resolver) {
            resolvers.put(type, resolver);
            return this;
        }
        
        public Builder configure(Consumer<DefaultConfiguration> consumer) {
            this.consumers.add(consumer);
            return this;
        }
        
        public DefaultConfiguration build() {
            DefaultConfiguration config = new DefaultConfiguration(
                    new DefaultSortedCompositePropertySource("root"),
                    propertySourceContext,
                    new DefaultTypeResolverRegistry(resolvers)
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
    public void addPropertySourceSpec(Layer layer, PropertySourceSpec bundle) {
        addPropertySource(layer, ImmutablePropertySource.builder()
            .named(bundle.getName())
            .putSources(
                bundle.getCascadeGenerator().apply(bundle.getName())
                .stream()
                .flatMap(name -> propertySourceContext.getFormats().stream()
                    .flatMap(factory -> propertySourceContext.getResolvers().stream()
                        .flatMap(resolver -> {
                            String interpolatedUrl = propertySourceContext.getInterpolator().apply(name + "." + factory.getExtension());
                            LOG.info("Loading url {}", interpolatedUrl);
                            return resolver
                                .apply(interpolatedUrl).stream()
                                .map(url -> factory.read(url, propertySourceContext))
                                .flatMap(optional -> optional.isPresent() ? Stream.of(optional.get()) : Stream.empty())
                                ;
                        })))
                .collect(Collectors.toList()))
            .build());
    }
    
    @Override
    public SortedCompositePropertySource getPropertySource() {
        return source;
    }
}
