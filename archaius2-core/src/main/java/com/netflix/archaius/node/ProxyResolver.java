package com.netflix.archaius.node;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.netflix.archaius.api.Resolver;
import com.netflix.archaius.api.ResolverLookup;
import com.netflix.archaius.api.PropertyNode;
import com.netflix.archaius.api.annotations.PropertyName;

/**
 * 
 * @param <T>
 * 
 * TODO: @DefaultValue
 */
public class ProxyResolver<T> implements Resolver<T> {
    private Class<T> type;
    private Function<Method, String> nameResolver = DEFAULT_NAME_RESOLVER;
    
    public ProxyResolver(Class<T> type) {
        this.type = type;
    }
    
    static interface MethodInvoker {
        Object invoke(Object... args);
    }
    
    @Override
    public T resolve(PropertyNode node, ResolverLookup resolvers) {
        final Map<Method, MethodInvoker> methods = new HashMap<>();
        for (Method method : type.getDeclaredMethods()) {
            final Object value = resolvers.get(method.getGenericReturnType())
                .resolve(node.getNode(nameResolver.apply(method)), resolvers);
            
            methods.put(method, new MethodInvoker() {
                @Override
                public Object invoke(Object... args) {
                    return value;
                }
            });
        }
        
        // Hack so that default interface methods may be called from a proxy
        final MethodHandles.Lookup temp;
        try {
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
                    .getDeclaredConstructor(Class.class, int.class);
            constructor.setAccessible(true);
            temp = constructor.newInstance(type, MethodHandles.Lookup.PRIVATE);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create temporary object for " + type.getTypeName(), e);
        }

        final InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                MethodInvoker invoker = methods.get(method);
                if (invoker != null) {
                    Object value = invoker.invoke(args);
                    if (value != null) {
                        return value;
                    } else if (method.isDefault()) {
                        return temp.unreflectSpecial(method, type)
                                .bindTo(proxy)
                                .invokeWithArguments(args);
                    } else {
                        return null;
                    }
                }
                
                if ("toString".equals(method.getName())) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(type.getSimpleName()).append("[");
                    methods.forEach((m, v) -> {
                        sb.append(nameResolver.apply(m)).append("='");
                        try {
                            sb.append(invoke(proxy, m, args));
                        } catch (Throwable e1) {
                            sb.append(e1.getMessage());
                        }
                        sb.append("'");
                        sb.append(", ");
                    });
                    sb.append("]");
                    return sb.toString();
                } else {
                    throw new NoSuchMethodError(method.getName() + " not found on interface " + type.getName());
                }
            }
        };
        return (T)Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, handler);
    }
    
    private static Function<Method, String> DEFAULT_NAME_RESOLVER = method -> {
        final PropertyName nameAnnot = method.getAnnotation(PropertyName.class); 
        if (nameAnnot != null) {
            return nameAnnot.name();
        }
        
        String name;
        if (method.getName().startsWith("get")) {
            name = method.getName().substring("get".length());
        } else if (method.getName().startsWith("is")) {
            name = method.getName().substring("is".length());
        } else if (method.getName().startsWith("with")) {
            name = method.getName().substring("with".length());
        } else {
            name = method.getName();
        }
        
        return decapitalize(name);
    };
    
    private static String decapitalize(String string) {
        if (string == null || string.length() == 0) {
            return string;
        }
        char c[] = string.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }


}
