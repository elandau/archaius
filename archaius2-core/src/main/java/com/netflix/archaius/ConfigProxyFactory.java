package com.netflix.archaius;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.event.ChangeEvent;

import com.netflix.archaius.api.BeanFactory;
import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.Decoder;
import com.netflix.archaius.api.PropertyFactory;
import com.netflix.archaius.api.annotations.Configuration;
import com.netflix.archaius.api.annotations.DefaultValue;
import com.netflix.archaius.api.annotations.PropertyName;
import com.netflix.archaius.source.InterpolatingPropertySource;

/**
 * Factory for binding a configuration interface to properties in a {@link PropertyFactory}
 * instance.  Getter methods on the interface are mapped by naming convention
 * by the property name may be overridden using the @PropertyName annotation.
 * 
 * For example,
 * <pre>
 * {@code 
 * {@literal @}Configuration(prefix="foo")
 * interface FooConfiguration {
 *    int getTimeout();     // maps to "foo.timeout"
 *    
 *    String getName();     // maps to "foo.name"
 * }
 * }
 * </pre>
 * 
 * Default values may be set by adding a {@literal @}DefaultValue with a default value string.  Note
 * that the default value type is a string to allow for interpolation.  Alternatively, methods can  
 * provide a default method implementation.  Note that {@literal @}DefaultValue cannot be added to a default
 * method as it would introduce ambiguity as to which mechanism wins.
 * 
 * For example,
 * <pre>
 * {@code
 * {@literal @}Configuration(prefix="foo")
 * interface FooConfiguration {
 *    @DefaultValue("1000")
 *    int getReadTimeout();     // maps to "foo.timeout"
 *    
 *    default int getWriteTimeout() {
 *        return 1000;
 *    }
 * }
 * }
 * 
 * To create a proxy instance,
 * <pre>
 * {@code 
 * FooConfiguration fooConfiguration = configProxyFactory.newProxy(FooConfiguration.class);
 * }
 * </pre>
 * 
 * To override the prefix in {@literal @}Configuration or provide a prefix when there is no 
 * @Configuration annotation simply pass in a prefix in the call to newProxy.
 * 
 * <pre>
 * {@code 
 * FooConfiguration fooConfiguration = configProxyFactory.newProxy(FooConfiguration.class, "otherprefix.foo");
 * }
 * </pre>
 * 
 * By default all properties are dynamic and can therefore change from call to call.  To make the
 * configuration static set the immutable attributes of @Configuration to true.
 * 
 * Note that an application should normally have just one instance of ConfigProxyFactory
 * and PropertyFactory since PropertyFactory caches {@link com.netflix.archaius.api.Property} objects.
 * 
 * @see {@literal }@Configuration
 */
public class ConfigProxyFactory implements BeanFactory {

    /**
     * The decoder is used for the purpose of decoding any @DefaultValue annotation
     */
    private final ResolvingPropertySource resolver;
    private Function<Object, Object> interpolator;
    
    public ConfigProxyFactory(ResolvingPropertySource resolver) {
        this.resolver = resolver;
        this.interpolator = InterpolatingPropertySource.createInterpolator(resolver.getPropertySource());
    }
    
    @Deprecated
    public ConfigProxyFactory(Config config, Decoder decoder, DefaultPropertyFactory defaultPropertyFactory) {
        this(new ResolvingPropertySource(config, new StringConverterRegistryBuilder().build()));
    }

    public ResolvingPropertySource getPropertyResolver() {
        return resolver;
    }
    
    /**
     * Create a proxy for the provided interface type for which all getter methods are bound
     * to a Property.
     * 
     * @param type
     * @param config
     * @return
     */
    @Override
    public <T> T createInstance(final Class<T> type) {
        return newProxy(type, null);
    }
    
    public <T> T createInstance(final Class<T> type, String prefix) {
        return newProxy(type, prefix);
    }
    
    public <T> T newProxy(final Class<T> type) {
        return createInstance(type);
    }
    
    private String derivePrefix(Configuration annot, String prefix) {
        if (prefix == null && annot != null) {
            prefix = annot.prefix();
            if (prefix == null) {
                prefix = "";
            }
        }
        if (prefix == null) 
            return "";
        
        if (prefix.endsWith(".") || prefix.isEmpty())
            return prefix;
        
        return prefix + ".";
    }
    
    public <T> T newProxy(final Class<T> type, final String initialPrefix) {
        Configuration annot = type.getAnnotation(Configuration.class);
        return createInstance(type, initialPrefix, annot == null ? false : annot.immutable());
    }
    
    private String methodToPropertyName(Method method) {
        final PropertyName nameAnnot = method.getAnnotation(PropertyName.class); 
        if (nameAnnot != null) {
            return nameAnnot.name();
        }
        
        final String verb;
        if (method.getName().startsWith("get")) {
            verb = "get";
        } else if (method.getName().startsWith("is")) {
            verb = "is";
        } else {
            verb = "";
        }
        
        return Character.toLowerCase(method.getName().charAt(verb.length())) + method.getName().substring(verb.length() + 1);

    }
    
    static class KeyAndValue {
        public KeyAndValue(String key, Object value) {
            this.key = key;
            this.value = value;
        }
        
        final String key;
        final Object value;
    }
    
    private static <T> Supplier<T> memoize(Supplier<T> supplier) {
        T value = supplier.get();
        return () -> value;
    }
    
    @SuppressWarnings({ "unchecked" })
    <T> T createInstance(final Class<T> type, final String initialPrefix, boolean immutable) {
        Configuration annot = type.getAnnotation(Configuration.class);
        final String prefix = derivePrefix(annot, initialPrefix);
        
        final Map<Method, KeyAndValue> methodAndDefaultValues = Arrays
                .stream(type.getMethods())
                .collect(Collectors.toMap(
                        method -> method,
                        m -> {
                            try {
                                final String propName = prefix + methodToPropertyName(m);
                                
                                final Class<?> returnType = m.getReturnType();
                                
                                Object defaultValue = null;
                                if (m.getAnnotation(DefaultValue.class) != null) {
                                    if (m.isDefault()) {
                                        throw new IllegalArgumentException("@DefaultValue cannot be defined on a method with a default implementation for method "
                                                + m.getDeclaringClass().getName() + "#" + m.getName());
                                    }
                                    
                                    // TODO: Test number interpolation for default value.  Ex.  @DefaultValue("${some.expected.integer:123}")
                                    String value = m.getAnnotation(DefaultValue.class).value();
                                    defaultValue = resolver.resolve(interpolator.apply(value).toString(), returnType);
                                } 
                                
                                if (defaultValue == null && m.getGenericReturnType() instanceof ParameterizedType && !m.isDefault()) {
                                    ParameterizedType pType = (ParameterizedType) m.getGenericReturnType();
                                    if (pType.getRawType() == Map.class) {
                                        defaultValue = Collections.emptyMap();
                                    } else if (pType.getRawType() == List.class) {
                                        defaultValue = Collections.emptyList();
                                    } else if (pType.getRawType() == LinkedList.class) {
                                        defaultValue = new LinkedList();
                                    } else if (pType.getRawType() == Set.class) {
                                        defaultValue = Collections.emptySet();
                                    } else if (pType.getRawType() == SortedSet.class) {
                                        defaultValue = Collections.emptySortedSet();
                                    }
                                }
                                
                                return new KeyAndValue(propName, defaultValue);
                                
                                // TODO: Support sub-interfaces?
                                // TODO: Support sub-interfaces in a Map?!
                                // TODO: Update values?
                            } catch (Exception e) {
                                throw new RuntimeException("Error proxying method " + m.getName(), e);
                            }
                        }
                    )
                );
        
        // Iterate through all declared methods of the class looking for setter methods.
        // Each setter will be mapped to a Property<T> for the property name:
        //      prefix + lowerCamelCaseDerivedPropertyName
        final Supplier<Map<Method, KeyAndValue>> entitySupplier = () -> {
            IdentityHashMap<Method, KeyAndValue> values = new IdentityHashMap<>(methodAndDefaultValues.size());
            methodAndDefaultValues.forEach((method, keyAndDefault) ->
                values.put(
                    method, 
                    new KeyAndValue(keyAndDefault.key, resolver.getProperty(keyAndDefault.key, method.getGenericReturnType()).orElse(keyAndDefault.value)))
            );
            return values;
        };
        
        final Supplier<Map<Method, KeyAndValue>> methods = annot == null || !annot.immutable()
                ? entitySupplier
                : memoize(entitySupplier);
        
        this.resolver.getPropertySource().addChangeEventListener((event) -> {
            
        });
        
        // Hack so that default interface methods may be called from a proxy
        final MethodHandles.Lookup temp;
        try {
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
                    .getDeclaredConstructor(Class.class, int.class);
            constructor.setAccessible(true);
            temp = constructor.newInstance(type, MethodHandles.Lookup.PRIVATE);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create temporary object for " + type.getName(), e);
        }
        
        final InvocationHandler handler = (proxy, method, args) -> {
            KeyAndValue keyAndValue = methods.get().get(method);
            if (keyAndValue != null) {
                if (keyAndValue.value == null && method.isDefault()) {
                    return temp.unreflectSpecial(method, type)
                            .bindTo(proxy)
                            .invokeWithArguments();
                } else {
                    return keyAndValue.value;
                }
            }
            
            if ("toString".equals(method.getName())) {
                StringBuilder sb = new StringBuilder();
                sb.append(type.getSimpleName()).append("[");
                methods.get().values().forEach(kv -> {
                    sb.append(kv.key.substring(prefix.length()))
                      .append("='")
                      .append(kv.value)
                      .append("', ");
                });
                sb.append("]");
                return sb.toString();
            } else {
                throw new NoSuchMethodError(method.getName() + " not found on interface " + type.getName());
            }
        };
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, handler);
    }
    
    private static class CachedSupplier<T> implements Consumer<ChangeEvent> {
        private static int CLEAN = 0;
        
        private final Supplier<T> source;
        private AtomicStampedReference<T> cache;
        
        public CachedSupplier(Supplier<T> source) {
            this.source = source;
            this.cache = new AtomicStampedReference<>(source.get(), 0);
        }
        
        @Override
        public void accept(ChangeEvent t) {
            cache.set(newReference, newStamp);
        }
    }
}
