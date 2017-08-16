package com.netflix.archaius.api;

/**
 * Contract for creating a type and mapping configuration to it.
 * 
 */
public interface BeanFactory {
    /**
     * Construct a new instance of type and map configuration to it.
     * A proxy object will be created if type is an interface 
     * 
     * @param type
     * @return Newly constructed object of the requested type with configuration
     *  mapped to it.
     */
    default <T> T createInstance(Class<T> type) {
        return createInstance(type, "");
    }
    
    /**
     * Construct a new instance of type and map configuration to it.
     * A proxy object will be created if type is an interface 
     * 
     * @param type
     * @return Newly constructed object of the requested type with configuration
     *  mapped to it.
     */
    <T> T createInstance(Class<T> type, String prefix);
}
