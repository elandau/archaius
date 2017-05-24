package com.netflix.config.sources.formats;

import com.netflix.config.api.PropertySource;
import com.netflix.config.sources.ImmutablePropertySource;
import com.netflix.config.sources.InterpolatingPropertySource;
import com.netflix.config.sources.PropertySources;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class PropertySourceFactoryContext {
    private static final UrlResolver SYSTEM_RESOURCES_RESOLVER = name -> {
        try {
            return Collections.list(ClassLoader.getSystemResources(name));
        } catch (Exception e) {
            return Collections.emptyList();
        }
    };

    public interface UrlResolver extends Function<String, List<URL>> {
    }

    public static PropertySourceFactoryContext DEFAULT = new PropertySourceFactoryContext();
    
    private final Function<String, String> interpolator;

    private final List<PropertySourceFormat> formats;
    
    private final List<UrlResolver> resolvers;

    PropertySourceFactoryContext() {
        this(new InterpolatingPropertySource(
                ImmutablePropertySource.builder()
                    .putSource(PropertySources.environment())
                    .putSource(PropertySources.system())
                    .build()),
             Arrays.asList(PropertiesPropertySourceFormat.INSTANCE, YamlPropertySourceFormat.INSTANCE),
             Arrays.asList(SYSTEM_RESOURCES_RESOLVER));
    }
    
    PropertySourceFactoryContext(Function<String, String> interpolator, List<PropertySourceFormat> formats, List<UrlResolver> resolvers) {
        this.interpolator = interpolator;
        this.formats = formats;
        this.resolvers = resolvers;
    }
    
    /**
     * Include an additional files format
     * @param factory
     * @return Chainable Options
     */
    public PropertySourceFactoryContext withFormat(PropertySourceFormat factory) {
        List<PropertySourceFormat> formats = new ArrayList<>(this.formats);
        formats.add(factory);
        return new PropertySourceFactoryContext(interpolator, formats, resolvers);
    }
    
    /**
     * Property source to use for interpolation
     * @param source
     * @return Chainable Options
     */
    public PropertySourceFactoryContext withInterpolator(Function<String, String> interpolator) {
        return new PropertySourceFactoryContext(interpolator, formats, resolvers);
    }
    
    /**
     * Additional UrlResolver to use for discovering the InputStream for a string name
     * 
     * @param resolver
     * @return Chainable Options
     */
    public PropertySourceFactoryContext withURLResolver(UrlResolver resolver) {
        List<UrlResolver> resolvers = new ArrayList<>(this.resolvers);
        resolvers.add(resolver);
        return new PropertySourceFactoryContext(interpolator, formats, resolvers);
    }
    
    /**
     * @return Return the {@link PropertySource} for interpolation
     */
    public Function<String, String> getInterpolator() {
        return interpolator;
    }
    
    /**
     * @return List of supported property file formats
     */
    public List<PropertySourceFormat> getFormats() {
        return formats;
    }
    
    public List<UrlResolver> getResolvers() {
        return resolvers;
    }
}