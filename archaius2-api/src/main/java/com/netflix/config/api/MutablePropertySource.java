package com.netflix.config.api;

import java.util.Map;

/**
 * Contract for a {@link PropertySource} that may be mutated.
 */
public interface MutablePropertySource extends PropertySource {
    /**
     * Set a property value.  If the value already exists it will be overwritten.
     * 
     * @param key
     * @param value
     * @return Old value or null if not set
     */
    Object setProperty(String key, Object value);

    /**
     * Set multiple properties in one operations.  This API is preferable when setting
     * a large number of operations as it is expected do be done atomically.
     * @param values
     */
    void setProperties(Map<String, Object> values);

    /**
     * Clear the value of a property
     * 
     * @param key
     * @return Old value or null if not set
     */
    Object clearProperty(String key);

    /**
     * Clear all property values
     */
    void clearProperties();

}
