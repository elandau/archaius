package com.netflix.config.sources.formats;

import com.netflix.config.api.PropertySource;

import java.net.URL;
import java.util.Optional;

/**
 * Contract for loading a properties from a URL into a PropertySource, normally
 * an instance of ImmutablePropertySource  
 */
public interface PropertySourceFormat {
    /**
     * Return the preferred extension for this file format
     * 
     * TODO: Should this be a list?
     * 
     * @return
     */
    String getExtension();
    
    /**
     * Read a URL into an immutable PropertySource
     * 
     * @param url
     * @param options
     * @return
     */
    Optional<PropertySource> read(URL url, PropertySourceFactoryContext options);
}
