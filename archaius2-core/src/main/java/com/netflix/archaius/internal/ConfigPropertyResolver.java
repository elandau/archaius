package com.netflix.archaius.internal;

import java.util.Optional;

import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.PropertyResolver;

public class ConfigPropertyResolver implements PropertyResolver {

    private Config config;

    public ConfigPropertyResolver(Config config) {
        this.config = config;
    }

    @Override
    public <T> Optional<T> getProperty(String key, Class<T> type) {
        return Optional.ofNullable(config.get(type, key, null));
    }
}
