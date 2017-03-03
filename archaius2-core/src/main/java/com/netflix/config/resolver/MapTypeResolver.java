package com.netflix.config.resolver;

import com.netflix.config.api.ConfigurationNode;
import com.netflix.config.api.TypeResolver;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class MapTypeResolver implements TypeResolver<Map<Object, Object>> {
    private final Type keyType;
    private final Type valueType;
    
    public MapTypeResolver(Type keyType, Type valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }
    
    @Override
    public Map<Object, Object> resolve(ConfigurationNode node, Registry resolvers) {
        return Collections.unmodifiableMap(node.children().stream()
            .map(key -> {
                System.out.println("key: " + key);
                int index = key.indexOf(".");
                return index == -1 ? key : key.substring(0, index);
            })
            .distinct()
            .collect(Collectors.toMap(
                key -> key,
                key -> resolvers.get(valueType).resolve(node.getChild(key), resolvers))
            ));
    }
}
