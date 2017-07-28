package com.netflix.archaius.api;

import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Contract for a raw source of properties
 */
public interface PropertySource extends ChangeEventSource {
    /**
     * Get the raw property value.  No interpolation or other modification is done to the property.
     * @param key
     */
    Optional<Object> getProperty(String key);

    /**
     * Mechanism for consuming all properties of the PropertySource
     * @param consumer
     */
    void forEachProperty(BiConsumer<String, Object> consumer);

    /**
     * @return Name used to identify the source such as a filename.
     */
    default String getName() { return "unknown"; }

    /**
     * @return True if empty or false otherwise.
     */
    boolean isEmpty();

    /**
     * @return Keys for all properties in the PropertySource
     */
    Iterable<String> getPropertyNames();
}
