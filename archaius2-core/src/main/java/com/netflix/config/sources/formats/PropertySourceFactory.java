package com.netflix.config.sources.formats;

import com.netflix.archaius.interpolate.CommonsStrInterpolator;
import com.netflix.config.api.Bundle;
import com.netflix.config.api.PropertySource;
import com.netflix.config.sources.CompositePropertySource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Function for loading a PropertySource for a resource {@link Bundle}.  The returned PropertySource
 * will contain properties from all found cascade resource name variants for all supported formats. 
 * Note that if multiple files exist, all will be loaded.
 */
public class PropertySourceFactory implements Function<Bundle, PropertySource> {
    private static final Logger LOG = LoggerFactory.getLogger(PropertySourceFactory.class);
    
    private final Map<String, Function<URL, PropertySource>> extensionToFactory = new HashMap<>();
    private final List<Function<String, List<URL>>> urlResolvers = new ArrayList<>();

    private final Function<String, String> interpolator;
    
    public PropertySourceFactory(PropertySource root) {
        this.interpolator = CommonsStrInterpolator.forPropertySource(root);
        
        // Supported file formats
        extensionToFactory.put("properties", new PropertiesToPropertySource());
        extensionToFactory.put("yml",        new YamlToPropertySource(root));
        
        // Resolvers from name to URL
        urlResolvers.add(name -> {
            try {
                return Collections.list(ClassLoader.getSystemResources(name));
            } catch (Exception e) {
                return Collections.emptyList();
            }
        });
    }
    
    @Override
    public PropertySource apply(Bundle bundle) {
        return new CompositePropertySource(
            bundle.getName(), 
            bundle.getCascadeGenerator().apply(bundle.getName()).stream()
                // All resource name variants (without extension)
                .flatMap(name -> extensionToFactory.entrySet().stream()
                    // All supported file formats
                    .flatMap(factory -> urlResolvers.stream()
                        // Load each file
                        .flatMap(resolver -> {
                            String interpolatedUrl = interpolator.apply(name + "." + factory.getKey());
                            LOG.info("Loading url {}", interpolatedUrl);
                            return resolver.apply(interpolatedUrl).stream()
                                .map(url -> factory.getValue().apply(url));
                        })
                    )
                )
                .collect(Collectors.toList()));
    }
}
