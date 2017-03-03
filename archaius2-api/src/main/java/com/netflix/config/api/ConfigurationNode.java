package com.netflix.config.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Inner node within a configuration hierarchy that references a subset of properties
 * with a specific prefix.
 */
public interface ConfigurationNode {
    default Optional<?> getValue() {
        return Optional.empty();
    }
    
    default ConfigurationNode getChild(String key) {
        return new ConfigurationNode() {};
    }
    
    default Collection<String> children() {
        return Collections.emptyList();
    }
}
