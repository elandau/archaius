package com.netflix.config.api;

import com.netflix.config.api.SortedCompositePropertySource.Layer;

public interface Configuration {
    void addPropertySource(Layer layer, PropertySource source);
    
    void addBundle(Layer layer, Bundle bundle);
    
    PropertyResolver getPropertyResolver();
    
    SortedCompositePropertySource getPropertySource();
}
