package com.netflix.config.api;

/**
 * Top level configuration API users interact with.  
 */
public interface Configuration {
    /**
     * Add a PropertySource at a specific Layer
     * 
     * @param layer
     * @param source
     */
    void addPropertySource(Layer layer, PropertySource source);
    
    /**
     * Add a named bundle 
     * 
     * @param layer
     * @param spec
     */
    void addPropertySourceSpec(Layer layer, PropertySourceSpec spec);
    
    /**
     * 
     * @return
     */
    PropertyResolver getPropertyResolver();

    /**
     * @return PropertySource backing this configuration
     */
    PropertySource getPropertySource();
}
