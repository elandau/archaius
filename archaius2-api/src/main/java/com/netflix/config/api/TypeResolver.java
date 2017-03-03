package com.netflix.config.api;

import java.lang.reflect.Type;

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
    
        <T> TypeResolver<T> get(Type type);
    
        default <T> TypeResolver<T> forType(Class<T> type) {
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
    T resolve(ConfigurationNode node, Registry registry);
}