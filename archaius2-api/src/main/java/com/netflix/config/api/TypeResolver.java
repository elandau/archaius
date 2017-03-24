package com.netflix.config.api;

import java.lang.reflect.Type;
import java.util.NoSuchElementException;

/**
 * Contract for resolving a type from a ConfigurationNode.  This could be a value derived from a single
 * property value or a complex object derived from multiple properties
 * 
 * @param <T>
 */
public interface TypeResolver<T> {
    
    /**
     * Registry of known type resolvers
     */
    public interface Registry {
        /**
         * Return a {@link TypeResolver} for type 'type' or throw a {@link NoSuchElementException}
         * 
         * @param type
         * @return
         */
        <T> TypeResolver<T> get(Type type);
    
        /**
         * Return a {@link TypeResolver} for type 'type' or throw a {@link NoSuchElementException}
         * 
         * @param type
         * @return
         */
        default <T> TypeResolver<T> get(Class<T> type) {
            return get((Type)type);
        }
        
    }

    /**
     * Resolve a type from the provided {@link ConfigurationNode}.  A {@link Registry} is provided
     * so sub fields may be resolved when constructing complex objects.
     * 
     * @param node ConfigurationNode in the property tree from where the value will be resolved.  
     *      This could be a leaf node for single value types or an inner node for Maps and complex types.
     * @param registry Resolve 
     * @return Resolved type
     */
    T resolve(String path, PropertySource source, TypeResolver.Registry resolvers);
    
    T resolve(Object value, TypeResolver.Registry resolvers);
}