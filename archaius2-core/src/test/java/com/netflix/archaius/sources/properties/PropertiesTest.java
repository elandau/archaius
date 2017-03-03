package com.netflix.archaius.sources.properties;

import com.netflix.config.sources.formats.PropertiesToPropertySource;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class PropertiesTest {
    @Test
    public void loadExisting() {
        Map<String, Object> properties = new HashMap<>();
        
        Stream.of("test.properties")
            .map(ClassLoader::getSystemResource)
            .map(new PropertiesToPropertySource())
            .forEach(source -> source.forEach((key, value) -> properties.put(key, value)));
        
        Assert.assertEquals(4,  properties.size());
    }
    
    @Test
    public void ignoreMissingFile() {
        Map<String, Object> properties = new HashMap<>();
        Stream.of("missing.properties")
            .map(ClassLoader::getSystemResource)
            .filter(url -> url != null)
            .map(new PropertiesToPropertySource())
            .forEach(source -> source.forEach((key, value) -> properties.put(key, value)));
        
        Assert.assertTrue(properties.isEmpty());
    }
}
