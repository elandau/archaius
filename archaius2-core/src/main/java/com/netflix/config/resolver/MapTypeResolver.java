package com.netflix.config.resolver;

import com.netflix.config.api.PropertySource;
import com.netflix.config.api.TypeResolver;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MapTypeResolver implements TypeResolver<Map<Object, Object>> {
    private final Type keyType;
    private final Type valueType;
    
    public MapTypeResolver(Type keyType, Type valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }
    
    @Override
    public Map<Object, Object> resolve(String path, PropertySource source, Registry resolvers) {
        HashMap<Object, Object> map = new HashMap<>();
        
        TypeResolver<?> keyResolver = resolvers.get(keyType);
        TypeResolver<?> valueResolver = resolvers.get(valueType);
        
        source.forEach(path, (key, value) -> {
            String mapKey = path.isEmpty() ? key : key.substring(path.length() + 1);
            if (valueResolver.isStruct()) {
                int index = key.indexOf(".");
                mapKey = index == -1 ? key : key.substring(0, index);
            }
            
            map.put(keyResolver.resolve(mapKey, resolvers), valueResolver.resolve(value, resolvers));
        });
        
        return Collections.unmodifiableMap(map);
    }

    @Override
    public Map<Object, Object> resolve(Object value, TypeResolver.Registry resolvers) {
        TypeResolver<?> keyResolver = resolvers.get(keyType);
        TypeResolver<?> valueResolver = resolvers.get(valueType);
        
        HashMap<Object, Object> map = new LinkedHashMap<>();
        Arrays.stream(value.toString().split(";"))
            .map(String::trim)
            .filter(str -> !str.isEmpty())
            .forEach(pair -> {
                String[] parts = pair.split("=");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Invalid map string : " + value);
                }
                map.put(keyResolver.resolve(parts[0], resolvers), valueResolver.resolve(parts[1], resolvers));
            });
        return Collections.unmodifiableMap(map);
    }

    @Override
    public boolean isStruct() {
        return true;
    }
}
