package com.netflix.archaius.readers;

import com.netflix.archaius.ConfigReaderRegistry;
import com.netflix.archaius.api.ConfigReader;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class ServiceLoaderConfigReaderRegistry implements ConfigReaderRegistry {
    private List<ConfigReader> readers = new ArrayList<>();
    
    public ServiceLoaderConfigReaderRegistry() {
        ServiceLoader.load(ConfigReader.class).forEach(readers::add);
    }
    
    @Override
    public List<ConfigReader> getReaders() {
        return readers;
    }
}
