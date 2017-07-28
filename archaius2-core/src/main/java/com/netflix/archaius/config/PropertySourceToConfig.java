package com.netflix.archaius.config;

import com.netflix.archaius.ResolvingPropertySource;
import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.ConfigListener;
import com.netflix.archaius.api.PropertySource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PropertySourceToConfig implements Config {
    ResolvingPropertySource delegate;
    
    public PropertySourceToConfig(ResolvingPropertySource source) {
        delegate = source;
    }
    
    private PropertySource getPropertySource() {
        return delegate.getPropertySource();
    }
    
    private <T> T notFound(String key) {
        throw new NoSuchElementException("'" + key + "' not found");
    }

    @Override
    public boolean isEmpty() {
        return getPropertySource().isEmpty();
    }

    @Override
    public Iterable<String> getPropertyNames() {
        return getPropertySource().getPropertyNames();
    }

    @Override
    public void addChangeEventListener(Consumer<ChangeEvent> consumer) {
        getPropertySource().addChangeEventListener(consumer);
    }

    @Override
    public void addListener(ConfigListener listener) {
        addChangeEventListener(event -> {
            listener.onConfigUpdated(this);
        });
    }

    @Override
    public void removeListener(ConfigListener listener) {
        // TODO:
    }

    @Override
    public Object getRawProperty(String key) {
        return getPropertySource().getProperty(key).orElse(null);
    }

    @Override
    public Long getLong(String key) {
        return delegate.getLong(key).orElseThrow(() -> notFound(key));
    }

    @Override
    public Long getLong(String key, Long defaultValue) {
        return delegate.getLong(key).orElse(defaultValue);
    }

    @Override
    public String getString(String key) {
        return delegate.getString(key).orElseThrow(() -> notFound(key));
    }

    @Override
    public String getString(String key, String defaultValue) {
        return delegate.getString(key).orElse(defaultValue);
    }

    @Override
    public Double getDouble(String key) {
        return delegate.getDouble(key).orElseThrow(() -> notFound(key));
    }

    @Override
    public Double getDouble(String key, Double defaultValue) {
        return delegate.getDouble(key).orElse(defaultValue);
    }

    @Override
    public Integer getInteger(String key) {
        return delegate.getInteger(key).orElseThrow(() -> notFound(key));
    }

    @Override
    public Integer getInteger(String key, Integer defaultValue) {
        return delegate.getInteger(key).orElse(defaultValue);
    }

    @Override
    public Boolean getBoolean(String key) {
        return delegate.getBoolean(key).orElseThrow(() -> notFound(key));
    }

    @Override
    public Boolean getBoolean(String key, Boolean defaultValue) {
        return delegate.getBoolean(key).orElse(defaultValue);
    }

    @Override
    public Short getShort(String key) {
        return delegate.getShort(key).orElseThrow(() -> notFound(key));
    }

    @Override
    public Short getShort(String key, Short defaultValue) {
        return delegate.getShort(key).orElse(defaultValue);
    }

    @Override
    public BigInteger getBigInteger(String key) {
        return delegate.getBigInteger(key).orElseThrow(() -> notFound(key));
    }

    @Override
    public BigInteger getBigInteger(String key, BigInteger defaultValue) {
        return delegate.getBigInteger(key).orElse(defaultValue);
    }

    @Override
    public BigDecimal getBigDecimal(String key) {
        return delegate.getBigDecimal(key).orElseThrow(() -> notFound(key));
    }

    @Override
    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        return delegate.getBigDecimal(key).orElse(defaultValue);
    }

    @Override
    public Float getFloat(String key) {
        return delegate.getFloat(key).orElseThrow(() -> notFound(key));
    }

    @Override
    public Float getFloat(String key, Float defaultValue) {
        return delegate.getFloat(key).orElse(defaultValue);
    }

    @Override
    public Byte getByte(String key) {
        return delegate.getByte(key).orElseThrow(() -> notFound(key));
    }

    @Override
    public Byte getByte(String key, Byte defaultValue) {
        return delegate.getByte(key).orElse(defaultValue);
    }

    @Override
    public List<?> getList(String key) {
        return delegate.getList(key, Object.class).orElseThrow(() -> notFound(key));
    }

    @Override
    public <T> List<T> getList(String key, Class<T> type) {
//        return delegate.getList(key).orElse(defaultValue);
        return null;
    }

    @Override
    public List<?> getList(String key, List<?> defaultValue) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T get(Class<T> type, String key) {
        return delegate.getProperty(key, type).orElseThrow(() -> notFound(key));
    }

    @Override
    public <T> T get(Class<T> type, String key, T defaultValue) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean containsKey(String key) {
        return getPropertySource().getProperty(key).isPresent();
    }

    @Override
    public Iterator<String> getKeys() {
        return getPropertySource().getPropertyNames().iterator();
    }

    @Override
    public Iterator<String> getKeys(String prefix) {
//        return getPropertySource().su().iterator();
        return null;
    }

    @Override
    public Config getPrefixedView(String prefix) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        getPropertySource().forEachProperty((key, value) -> {
            visitor.visitKey(key, value);
        });
        return null;
    }

    @Override
    public Optional<Object> getProperty(String key) {
        return delegate.getPropertySource().getProperty(key);
    }

    @Override
    public void forEachProperty(BiConsumer<String, Object> consumer) {
        delegate.getPropertySource().forEachProperty(consumer);
    }

}
