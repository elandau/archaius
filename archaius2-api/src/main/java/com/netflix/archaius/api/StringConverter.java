package com.netflix.archaius.api;

import java.lang.reflect.Type;

/**
 * Mapping function to convert a string to a specific type.
 *
 * @param <T>
 */
public interface StringConverter<T> {
    /**
     * Registry of StringConverters
     */
    interface Registry {
        /**
         * @param type
         * @return Can be null
         */
        <T> StringConverter<T> get(Type type);
    }
    
    /**
     * Convert a string to a type
     * 
     * @param value
     * @return
     */
    T convert(String value);
}
