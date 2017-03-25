package com.netflix.archaius;

import com.netflix.config.api.Bundle;
import com.netflix.config.api.Bundle.CascadeGenerator;
import com.netflix.config.api.Layers;
import com.netflix.config.sources.ImmutablePropertySource;
import com.netflix.config.sources.PropertySources;
import com.netflix.config.sources.SynchronizedMutablePropertySource;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class ConfigurationTest {
    public static class MyCascadeStrategy implements CascadeGenerator {
        @Override
        public List<String> apply(String t) {
            return Arrays.asList(t, t + "-${env}");
        }
    }
    
    @Test
    public void test() {
        SynchronizedMutablePropertySource settable = new SynchronizedMutablePropertySource("test");
        
        DefaultConfiguration config = DefaultConfiguration.builder()
            .configure(c -> {
                c.addPropertySource(Layers.TEST, settable);
                c.addPropertySource(Layers.DEFAULTS, ImmutablePropertySource.builder()
                        .named("default")
                        .put("a", "123")
                        .put("env", "prod")
                        .build());
                c.addPropertySource(Layers.ENVIRONMENT, PropertySources.environment());
                c.addPropertySource(Layers.SYSTEM, PropertySources.system());
                c.addBundle(Layers.APPLICATION, Bundle.named("application").cascadeGenerator(MyCascadeStrategy.class));
                c.addBundle(Layers.LIBRARIES, Bundle.named("libA").cascadeGenerator(MyCascadeStrategy.class));
            })
            .build();
        
        settable.setProperty("foo", "bar");
        
//        config.getPropertySource().forEach(s -> {
//            System.out.println("Source : " + s);
//            s.forEach((key, value) -> System.out.println("  " + key + " : "+ value));
//        });
        
        System.out.println(config.getPropertyResolver().getMap("", String.class, String.class));
        Assert.assertEquals(123, config.getPropertyResolver().getInteger("a").get().intValue());
    }
}
