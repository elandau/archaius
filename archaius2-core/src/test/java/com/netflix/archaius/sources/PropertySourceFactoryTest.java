package com.netflix.archaius.sources;

import java.util.Arrays;

import org.junit.Test;

import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.Bundle;

public class PropertySourceFactoryTest {
    @Test
    public void loadVariants() {
        PropertySource source = ImmutablePropertySource.builder()
                .put("env", "prod")
                .build();
        
        PropertySourceFactory factory = new PropertySourceFactory(source);
        
        PropertySource loaded = factory.create(new Bundle("libA", (name) -> {
            return Arrays.asList(name, name + "-${env}");
        }));
        
        loaded.flattened().forEach(s -> System.out.println(s.getName()));
        
        loaded.forEach((key, value) -> System.out.println(key + " = " + value));
    }
}
