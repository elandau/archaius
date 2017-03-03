package com.netflix.config.sources.formats;

import com.netflix.config.api.PropertySource;

import java.net.URL;
import java.util.function.Function;

/**
 * Contract for loading a properties from a URL into a PropertySource, normally
 * an instance of ImmutablePropertySource  
 */
public interface PropertySourceLoader extends Function<URL, PropertySource> {

}
