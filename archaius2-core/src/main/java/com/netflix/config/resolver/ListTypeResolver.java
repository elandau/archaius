package com.netflix.config.resolver;

import com.netflix.config.api.PropertySource;
import com.netflix.config.api.TypeResolver;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ListTypeResolver implements TypeResolver<List<?>> {

    private Type elementType;
    
    public ListTypeResolver(Type elementType) {
        this.elementType = elementType;
    }
    
    @Override
    public List<?> resolve(String path, PropertySource source, Registry resolvers) {
        return source.getProperty(path).map(value -> {
            if (value instanceof String) {
                return resolve(value, resolvers);
            } else {
                throw new IllegalArgumentException();
            }
        }).orElse(Collections.emptyList());
    }
    
    @Override
    public List<?> resolve(Object value, TypeResolver.Registry registry) {
        TypeResolver<?> elementResolver = registry.get(elementType);
        
        if (value instanceof String) {
            return Arrays.stream(((String)value).split(","))
                .map(element -> elementResolver.resolve(element, registry))
                .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException();
        }
    }
}
