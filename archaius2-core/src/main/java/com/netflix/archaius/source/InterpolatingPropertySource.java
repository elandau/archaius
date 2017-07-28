package com.netflix.archaius.source;

import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.StrInterpolator;
import com.netflix.archaius.interpolate.CommonsStrInterpolator;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class InterpolatingPropertySource<T extends PropertySource> extends DelegatingPropertySource {

    public static Function<Object, Object> createInterpolator(PropertySource source) {
        final StrInterpolator.Lookup lookup = key -> source.getProperty(key).map(Object::toString).orElse(null);
        return value -> CommonsStrInterpolator.INSTANCE.create(lookup).resolve((String)value);
    }
    
    private final T delegate;
    private final Function<Object, Object> interpolator;
    
    public InterpolatingPropertySource(T delegate) {
        this.delegate = delegate;
        this.interpolator = createInterpolator(delegate);
    }
    
    public String resolve(String value) {
        return interpolator.apply(value).toString();
    }
    
    @Override
    public T delegate() {
        return delegate;
    }

    @Override
    public  Optional<Object> getProperty(String key) { 
        return delegate().getProperty(key).map(interpolator); 
    }

    @Override
    public  void forEachProperty(BiConsumer<String, Object> consumer) {
        delegate().forEachProperty((key, value) -> consumer.accept(key, interpolator));
    }
}
