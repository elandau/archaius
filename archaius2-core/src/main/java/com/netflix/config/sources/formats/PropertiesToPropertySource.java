package com.netflix.config.sources.formats;

import com.netflix.config.api.PropertySource;
import com.netflix.config.sources.ImmutablePropertySource;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * Load properties from a standard .properties file.  
 * 
 * TODO: Support @next
 */
public class PropertiesToPropertySource implements PropertySourceLoader {
    private static final String INCLUDE_KEY = "@next";
    
    @Override
    public PropertySource apply(URL url) {
        Properties props = new Properties();
        load(props, url, new HashSet<>());
        
        return ImmutablePropertySource.builder()
                .named(url.toExternalForm())
                .putAll(props)
                .build();
    }
    
    private static void load(Properties props, URL url, Set<URL> seen) {
        if (!seen.add(url)) {
            return;
        }
        
        try (InputStream is = url.openStream()) {
            props.load(is);
            
            Optional.ofNullable(props.remove(INCLUDE_KEY)).ifPresent(list -> {
                Arrays.stream(list.toString().split(","))
                    .map(String::trim)
                    .filter(next -> !next.isEmpty())
                    .map(PropertiesToPropertySource::createURL_Unchecked)
                    .forEach(next -> load(props, next, seen));
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
}
