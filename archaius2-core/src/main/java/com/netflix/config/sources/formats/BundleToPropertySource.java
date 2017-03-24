package com.netflix.config.sources.formats;

import com.netflix.config.api.Bundle;
import com.netflix.config.api.Bundle.CascadeGenerator;
import com.netflix.config.api.PropertySource;
import com.netflix.config.sources.ImmutablePropertySource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Function for loading a PropertySource for a resource {@link Bundle}.  The returned PropertySource
 * will contain properties from all found cascade resource name variants for all supported formats. 
 * Note that if multiple files exist, all will be loaded.
 */
public final class BundleToPropertySource {
    private static final Logger LOG = LoggerFactory.getLogger(BundleToPropertySource.class);
    
    public PropertySource load(Bundle bundle, PropertySourceFactoryContext options) {
        return ImmutablePropertySource.builder()
            .named(bundle.getName())
            .putSources(generateCascade(bundle.getName(), bundle.getCascadeGenerator())
                .stream()
                .flatMap(name -> options.getFormats().stream()
                    .flatMap(factory -> options.getResolvers().stream()
                        .flatMap(resolver -> {
                            String interpolatedUrl = options.getInterpolator().apply(name + "." + factory.getExtension());
                            LOG.info("Loading url {}", interpolatedUrl);
                            return resolver
                                .apply(interpolatedUrl).stream()
                                .map(url -> factory.read(url, options))
                                .flatMap(optional -> optional.isPresent() ? Stream.of(optional.get()) : Stream.empty())
                                ;
                        })))
                .collect(Collectors.toList()))
            .build();
    }
    
    public static List<String> generateCascade(String name, Class<? extends CascadeGenerator> generator) {
        try {
            return generator.newInstance().apply(name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
