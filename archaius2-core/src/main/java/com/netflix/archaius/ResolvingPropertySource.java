package com.netflix.archaius;

import com.netflix.archaius.api.PropertyResolver;
import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.StringConverter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

/**
 * Wrapper for a PropertySource that can resolve property value to specific types.
 * 
 * A common set of resolvers is provided by default.  Additional resolvers may be 
 * specified by passing in a manually constructed {@link StringConverter.Registry}.
 * See {@link StringConverterRegistryBuilder}
 */
public class ResolvingPropertySource implements PropertyResolver {
    private final PropertySource delegate;
    private final StringConverter.Registry stringConverterRegistry; 
    
    public ResolvingPropertySource(PropertySource delegate, StringConverter.Registry registry) {
        this.delegate = delegate;
        this.stringConverterRegistry = registry;
    }
    
    public ResolvingPropertySource(PropertySource delegate) {
        this.delegate = delegate;
        this.stringConverterRegistry = new StringConverterRegistryBuilder().build();
    }
    
    /**
     * @return The underlying PropertySource
     */
    public PropertySource getPropertySource() {
        return delegate;
    }
    
    @Override
    public <P> Optional<P> getProperty(String key, final Class<P> type) {
        return getProperty(key, (Type)type);
    }

    @Override
    public <T> Optional<List<T>> getList(String key, Class<T> type) {
        return null;
    }

    @Override
    public <T> T resolve(String value, Class<T> type) {
        return resolve(value, (Type)type);
    }

    @Override
    public <T> Optional<T> getProperty(String key, Type type) {
        StringConverter<T> converter = stringConverterRegistry.get(type);
        if (converter == null) {
            throw new IllegalArgumentException("No StringConverter for type '" + type + "' when parsing value for key '" + key + "'");
        }
        return delegate.getProperty(key).map(obj -> {
            if (obj.getClass().equals(type)) {
                return (T)obj;
            } else if (obj instanceof String) {
                return converter.convert((String)obj);
            } else {
                return converter.convert(obj.toString());
            }
        });
    }

    @Override
    public <T> T resolve(String value, Type type) {
        return (T) stringConverterRegistry.get(type).convert(value);
    }
}
