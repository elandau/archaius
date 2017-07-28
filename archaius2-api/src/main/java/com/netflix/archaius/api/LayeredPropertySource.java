package com.netflix.archaius.api;

import com.netflix.archaius.api.Config.Visitor;
import com.netflix.archaius.api.exceptions.ConfigException;

import java.util.Collection;

/**
 * Composite PropertySource where the override order is driven by Layer keys.
 */
public interface LayeredPropertySource extends PropertySource, CompositePropertySource {
    static interface LayeredVisitor<T> extends Visitor<T> {
        /**
         * Visit a PropertySource at the specified layer.  visiPropertySource is called
         * in override order
         *
         * @param layer
         * @param child
         * @return
         */
        T visitPropertySource(Layer layer, PropertySource propertySource);
    }
    
    /**
     * Add a PropertySource at the specified Layer.
     * 
     * <p>
     * This will trigger an onConfigUpdated event.
     *
     * @param layer
     * @param child
     * @throws ConfigException
     */
    void addPropertySource(Layer layer, PropertySource propertySource);
    
    void addPropertySource(Layer layer, PropertySource propertySource, int position);

    /**
     * Remove a named PropertySource from a layer
     * @param layer
     * @param name
     * @return
     */
    void removePropertySource(Layer layer, String name);
    
    /**
     * Return all property sources at a layer
     * @param layer
     * @return Immutable list of all property sources at the specified layer.
     */
    Collection<PropertySource> getPropertySourcesAtLayer(Layer layer);
}
