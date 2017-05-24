package com.netflix.config.sources.formats;

import com.netflix.config.api.PropertySource;
import com.netflix.config.sources.ImmutablePropertySource;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

/**
 * Load properties from a standard .properties file.  
 * 
 * TODO: Support @next
 */
public final class PropertiesPropertySourceFormat implements PropertySourceFormat {
    private static final List<String> INCLUDE_KEY = Arrays.asList("@next", "netflixconfiguration.properties.nextLoad");
    
    public static PropertiesPropertySourceFormat INSTANCE = new PropertiesPropertySourceFormat();
    
    private PropertiesPropertySourceFormat() {
    }
    
    @Override
    public Optional<PropertySource> read(URL url, PropertySourceFactoryContext options) {
        Function<String, String> interpolator = options.getInterpolator();
        
        Properties props = new Properties();
        load(props, url, new HashSet<>(), interpolator);
        
        return Optional.ofNullable(ImmutablePropertySource.builder()
                .named(url.toExternalForm())
                .putAll(props)
                .build());
    }
    
    private static void load(Properties props, URL url, Set<URL> seen, Function<String, String> interpolator) {
        if (!seen.add(url)) {
            return;
        }
        
        try (InputStream is = url.openStream()) {
            props.load(is);
            
            INCLUDE_KEY.forEach(includeKey -> {
                Optional.ofNullable((String)props.remove(includeKey))
                    .map(interpolator)
                    .ifPresent(list -> {
                        Arrays.stream(list.toString().split(","))
                            .map(String::trim)
                            .filter(next -> !next.isEmpty())
                            .map(interpolator)
                            .map(PropertiesPropertySourceFormat::createURL_Unchecked)
                            .forEach(next -> load(props, next, seen, interpolator));
                });
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from " + url.toExternalForm(), e);
        }
    }
    
    private static URL createURL_Unchecked(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String getExtension() {
        return "properties";
    }
}
