package com.netflix.config.resolver;

import com.netflix.config.api.PropertySource;
import com.netflix.config.api.TypeResolver;

import java.lang.reflect.Type;
import java.util.HashMap;
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
            key = key.substring(path.length() + 1);
            int index = key.indexOf(".");
            String mapKey = index == -1 ? key : key.substring(0, index);
            
            map.put(keyResolver.resolve(mapKey, resolvers), valueResolver.resolve(value, resolvers));
        });
        
        return map;
    }

    @Override
    public Map<Object, Object> resolve(Object value, TypeResolver.Registry resolvers) {
        throw new IllegalStateException();
    }
}
