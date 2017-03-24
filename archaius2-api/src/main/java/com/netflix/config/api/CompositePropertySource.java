package com.netflix.config.api;

import java.util.function.Consumer;

public interface CompositePropertySource extends PropertySource {
    /**
     * @return Stream with any child PropertySource instances used to construct
     *         this property source. For most PropertySource implementations
     *         this will be an empty stream.
     */
    void forEach(Consumer<PropertySource> consumer);
}
