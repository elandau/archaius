package com.netflix.config.resolver;

import com.netflix.archaius.StringConverterRegistry;
import com.netflix.config.api.PropertySource;
import com.netflix.config.api.TypeResolver;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultTypeResolverRegistry implements TypeResolver.Registry {
    private Map<Type, TypeResolver<?>> deserializers = new ConcurrentHashMap<>();
    
    public DefaultTypeResolverRegistry() {
        StringConverterRegistry.DEFAULT_CONVERTERS.forEach((type, converter) -> {
            deserializers.put(type, new TypeResolver<Object>() {
                @Override
                public Object resolve(String path, PropertySource source, TypeResolver.Registry resolvers) {
                    return source.getProperty(path).map(value -> resolve(value, resolvers)).orElse(null);
                }

                @Override
                public Object resolve(Object value, TypeResolver.Registry resolvers) {
                    if (value instanceof String) {
                        return converter.apply((String)value);
                    } else if (value.getClass() == type) {
                        return value;
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
            });
        });
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeResolver<T> get(Type type) {
        return (TypeResolver<T>) deserializers.computeIfAbsent(type, t -> {
            if (t instanceof Class) {
                Class<?> cls = (Class<?>)type;
                if (cls.isInterface()) {
                    return new ProxyTypeResolver<>(cls);
                }
                
                throw new IllegalArgumentException("Don't know how to map type " + type.getTypeName());
            }
            
            if (type instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) type;
                if (pType.getRawType() == Map.class) {
                    return new MapTypeResolver(pType.getActualTypeArguments()[0], pType.getActualTypeArguments()[1]);
                } else if (pType.getRawType() == List.class) {
                    return new ListTypeResolver(pType.getActualTypeArguments()[0]);
                } else if (pType.getRawType() == Set.class) {
                    return new SetTypeResolver(pType.getActualTypeArguments()[0]);
                }

                throw new IllegalArgumentException("Don't know how to map type " + type.getTypeName());
            }
            
            throw new IllegalArgumentException("Don't know how to map type " + type.getTypeName());
        });
    }
}
