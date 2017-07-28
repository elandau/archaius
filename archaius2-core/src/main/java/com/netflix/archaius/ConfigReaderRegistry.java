package com.netflix.archaius;

import com.netflix.archaius.api.ConfigReader;

import java.util.List;

/**
 * Registry of readers for all supported file formats
 */
public interface ConfigReaderRegistry {
    /**
     * Immutable list of all loaded config readers
     * @return
     */
    List<ConfigReader> getReaders();
}
