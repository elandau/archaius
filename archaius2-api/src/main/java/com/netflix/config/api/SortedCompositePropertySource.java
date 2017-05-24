package com.netflix.config.api;

import java.util.function.Function;

public interface SortedCompositePropertySource extends CompositePropertySource {

    SortedCompositePropertySource addPropertySource(Layer layer, PropertySource source);
    
    SortedCompositePropertySource addPropertySource(Layer layer, Function<SortedCompositePropertySource, PropertySource> loader);
}
