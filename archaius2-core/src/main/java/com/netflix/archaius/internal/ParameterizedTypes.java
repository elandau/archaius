package com.netflix.archaius.internal;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParameterizedTypes {
    static <K, V> ParameterizedType mapOf(Class<K> keyType, Class<V> valueType) {
        return create(null, Map.class, new Type[]{keyType, valueType});
    }

    static <T> ParameterizedType setOf(Class<T> type) {
        return create(null, Set.class, new Type[]{type});
    }
    
    static <T> ParameterizedType listOf(Class<T> type) {
        return create(null, List.class, new Type[]{type});
    }
    
    static ParameterizedType create(Type ownerType, Type rawType, Type[] arguments) {
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return arguments;
            }

            @Override
            public Type getRawType() {
                return rawType;
            }

            @Override
            public Type getOwnerType() {
                return ownerType;
            }
        };
    }
}
