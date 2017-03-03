package com.netflix.config.resolver;

import com.netflix.config.api.PropertySource;
import com.netflix.config.api.TypeResolver;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class SetTypeResolver implements TypeResolver<Set<?>> {

    private Type elementType;
    
    public SetTypeResolver(Type elementType) {
        this.elementType = elementType;
    }
    
    @Override
    public Set<?> resolve(String path, PropertySource source, TypeResolver.Registry resolvers) {
        return source.getProperty(path).map(value -> {
            if (value instanceof String) {
                return resolve(value, resolvers);
            } else {
                throw new IllegalArgumentException();
            }
        }).orElse(Collections.emptySet());
    }

    @Override
    public Set<?> resolve(Object value, TypeResolver.Registry registry) {
        TypeResolver<?> elementResolver = registry.get(elementType);
        
        if (value instanceof String) {
            return Arrays.stream(((String)value).split(","))
                .map(element -> elementResolver.resolve(element, registry))
                .collect(Collectors.toSet());
        } else {
            throw new IllegalArgumentException();
        }
    }
}
