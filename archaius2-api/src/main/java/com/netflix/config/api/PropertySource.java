package com.netflix.config.api;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Contract for a source of raw properties.  A PropertySource may be associated with a single
 * data source or is a composite of multiple PropertySource instances.  Values may be any object
 * including strings, boxed primitives, collections, pojos, list, etc.
 * 
 * Users are not expected to interact with PropertySource for reading configuration.  
 * 
 * @see Configuration
 */
public interface PropertySource {
    /**
     * @return Name of the property source.  This could be an arbitrary name or a file name from whence the
     * configuration was loaded
     */
    String getName();

    /**
     * @param key
     * @return Value for the property 
     */
    Optional<Object> getProperty(String key);
    
    /**
     * @return Immutable collection of all property names.  For dynamic PropertySources it's still possible
     * for a property name in this collection to no longer exist when getProperty is called.
     */
    Collection<String> getKeys();

    /**
     * @return Immutable collection of all property names starting with prefix.  For dynamic PropertySources
     * it's still possible for a property name in this collection to no longer exist when getProperty is called.
     */
    Collection<String> getKeys(String prefix);
    
    /**
     * @return True if there are no properties in the PropertySource
     */
    boolean isEmpty();
    
    int size();
    
    /**
     * Register a consumer that will be invoked for any updates to the PropertySource.  To avoid the complexity
     * of figuring out what exactly changed, especially when dealing with interpolation, the notification
     * mechanism simply informs the listener that something had changed.
     * @param listener
     * @return Runnable that unregisters the listener when its run() method is called. 
     */
    default AutoCloseable addListener(Consumer<PropertySource> listener) { return () -> {}; }
    
    /**
     * @return Stream of all entries of this PropertySource
     */
    void forEach(BiConsumer<String, Object> consumer);
    
    /**
     * @patah prefix
     * @return Stream of all entries of this PropertySource starting with the prefix
     */
    void forEach(String prefix, BiConsumer<String, Object> consumer);
    
    /**
     * @param prefix
     * @return A PropertySource that is a subset of all properties with the specified prefix
     */
    PropertySource subset(String prefix);
    
    /**
     * @return Return a PropertySource that provides tree-like traversal of the current PropertySource
     * snapshot state
     */
    PropertySource snapshot();
    
    /**
     * @return Stream with any child PropertySource instances used to construct this property source.
     * For most PropertySource implementations this will be an empty stream.
     */
    default Stream<PropertySource> children() {
        return Stream.empty();
    }

    /**
     * @return Flattened view of the child PropertySource instances backing this PropertySource.  The
     * resulting stream returns an ordered set of PropertySources.
     */
    default Stream<PropertySource> flattened() {
        return Stream.concat(Stream.of(this), children().flatMap(PropertySource::flattened));
    }
}
