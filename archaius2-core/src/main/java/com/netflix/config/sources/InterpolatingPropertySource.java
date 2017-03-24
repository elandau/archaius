package com.netflix.config.sources;

import com.netflix.archaius.api.StrInterpolator;
import com.netflix.archaius.interpolate.CommonsStrInterpolator;
import com.netflix.config.api.PropertySource;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Specialized {@link PropertySource} that interpolates replacements of the syntax ${} using other values
 * in this property source.
 */
public class InterpolatingPropertySource extends DelegatingPropertySource implements Function<String, String> {

    private final PropertySource delegate;
    private final StrInterpolator.Lookup lookup;
    
    public InterpolatingPropertySource(PropertySource delegate) {
        this.delegate = delegate;
        this.lookup = key -> getProperty(key).map(Object::toString).orElse(null);
    }
    
    private Object resolve(Object value) {
        if (value.getClass() == String.class) {
            return CommonsStrInterpolator.INSTANCE.create(lookup).resolve((String)value);
        }
        return value;
    }
        
    @Override
    protected PropertySource delegate() {
        return delegate;
    }
    
    @Override
    public Optional<Object> getProperty(String key) {
        return delegate().getProperty(key).map(this::resolve);
    }
    
    @Override
    public void forEach(BiConsumer<String, Object> consumer) {
        delegate().forEach((key, value) -> {
            consumer.accept(key, resolve(value));
        });
    }

    @Override
    public void forEach(String prefix, BiConsumer<String, Object> consumer) {
        delegate().forEach(prefix, (key, value) -> {
            consumer.accept(key, resolve(value));
        });
    }

    @Override
    public PropertySource snapshot() {
        return new InterpolatingPropertySource(delegate.snapshot());
    }

    @Override
    public String apply(String value) {
        return (String) resolve(value);
    }
}
