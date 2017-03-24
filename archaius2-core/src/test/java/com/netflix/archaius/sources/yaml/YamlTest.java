package com.netflix.archaius.sources.yaml;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import com.netflix.config.api.PropertySource;
import com.netflix.config.sources.EmptyPropertySource;
import com.netflix.config.sources.ImmutablePropertySource;
import com.netflix.config.sources.formats.PropertySourceFactoryContext;
import com.netflix.config.sources.formats.YamlPropertySourceFormat;

public class YamlTest {
    @Test
    public void loadExisting() {
        PropertySource environment = ImmutablePropertySource.builder()
                .build();
        
        PropertySource yamlSource = Stream.of("loadtest.yml")
                .map(ClassLoader::getSystemResource)
                .map(url -> YamlPropertySourceFormat.INSTANCE.read(url, PropertySourceFactoryContext.DEFAULT).get())
                .findFirst().get();
        
        Assert.assertEquals(11,  yamlSource.size());
        Assert.assertEquals(7000, yamlSource.getProperty("application.port").get());
    }

    @Test
    public void loadExistingWithTest() {
        PropertySource environment = ImmutablePropertySource.builder()
                .put("env", "test")
                .build();
        
        PropertySource yamlSource = Stream.of("loadtest.yml")
                .map(ClassLoader::getSystemResource)
                .map(url -> YamlPropertySourceFormat.INSTANCE.read(url, PropertySourceFactoryContext.DEFAULT).get())
                .findFirst().get();
        
        Assert.assertEquals(11,  yamlSource.size());
        Assert.assertEquals(7001, yamlSource.getProperty("application.port").get());
    }
    
    @Test
    public void loadExistingWithProd() {
        PropertySource environment = ImmutablePropertySource.builder()
                .put("env", "prod")
                .build();
        
        PropertySource yamlSource = Stream.of("loadtest.yml")
                .map(ClassLoader::getSystemResource)
                .map(url -> YamlPropertySourceFormat.INSTANCE.read(url, PropertySourceFactoryContext.DEFAULT).get())
                .findFirst().get();
        
        Assert.assertEquals(11,  yamlSource.size());
        Assert.assertEquals(7002, yamlSource.getProperty("application.port").get());
    }

    @Test
    public void ignoreMissingFile() {
        Map<String, Object> properties = new HashMap<>();
        Stream.of("missing.yml")
            .map(ClassLoader::getSystemResource)
            .filter(url -> url != null)
            .map(url -> YamlPropertySourceFormat.INSTANCE.read(url, PropertySourceFactoryContext.DEFAULT).get())
            .forEach(source -> source.forEach((key, value) -> properties.put(key, value)));
        
        Assert.assertTrue(properties.isEmpty());
    }
}
