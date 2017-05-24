package com.netflix.config.sources;

import com.netflix.config.api.PropertySource;

public abstract class PropertySources {
    private PropertySources() {
    }
    
    private static ImmutablePropertySource SYSTEM_PROPERTIES = ImmutablePropertySource.builder()
            .named("system")
            .putAll(System.getProperties())
            .build();
    
    private static ImmutablePropertySource ENV_PROPERTIES = ImmutablePropertySource.builder()
            .named("env")
            .putAll(System.getenv())
            .build();

    public static PropertySource system() {
        return SYSTEM_PROPERTIES;
    }
    
    public static PropertySource environment() {
        return ENV_PROPERTIES;
    }

    public static PropertySource empty() {
        return EmptyPropertySource.INSTANCE;
    }
}
