package com.netflix.config.resolver;

import com.netflix.config.api.ConfigurationNode;
import com.netflix.config.api.TypeResolver;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ListTypeResolver implements TypeResolver<List<?>> {

    private Type elementType;
    
    public ListTypeResolver(Type elementType) {
        this.elementType = elementType;
    }
    
    @Override
    public List<?> resolve(ConfigurationNode node, Registry resolvers) {
        return node.getValue().map(value -> {
            if (value instanceof String) {
                return Collections.unmodifiableList(Arrays.asList(((String)value).split(","))
                    .stream()
                    .map(element -> resolvers.get(elementType).resolve(new ConfigurationNode() {
                        @Override
                        public Optional<?> getValue() {
                            return Optional.of(element);
                        }
                    }, resolvers))
                    .collect(Collectors.toList()));
            } else {
                throw new IllegalArgumentException();
            }
        }).orElse(Collections.emptyList());
    }
}
