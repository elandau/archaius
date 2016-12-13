package com.netflix.config.sources;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.netflix.archaius.internal.WeakReferenceSet;
import com.netflix.config.api.PropertySource;

public class MutablePropertySource implements PropertySource {
    private volatile SortedMap<String, Object> properties;
    private final String name;
    private final WeakReferenceSet<Consumer<PropertySource>> listeners = new WeakReferenceSet<>();

    public MutablePropertySource(String name) {
        this.properties = Collections.emptySortedMap();
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }

    private void internalSetProperties(SortedMap<String, Object> newProperties) {
        this.properties = Collections.unmodifiableSortedMap(newProperties);
        notifyListeners();
    }
    
    public synchronized Object setProperty(String key, Object value) {
        SortedMap<String, Object> newProperties = new TreeMap<>(properties);
        Object oldValue = newProperties.put(key, value);
        
        if (oldValue == null || !oldValue.equals(value)) {
            internalSetProperties(newProperties);
        }
        return oldValue;
    }
    
    public synchronized void setProperties(Map<String, Object> values) {
        SortedMap<String, Object> newProperties = new TreeMap<>(properties);
        values.forEach((k, v) -> newProperties.put(k, v));
        internalSetProperties(newProperties);
    }
    
    public synchronized Object clearProperty(String key) {
        if (!properties.containsKey(key)) {
            return null;
        }
        
        SortedMap<String, Object> newProperties = new TreeMap<>(properties);
        Object oldValue = newProperties.remove(key);
        internalSetProperties(newProperties);
        return oldValue;
    }

    public synchronized void clearProperties() {
        if (!properties.isEmpty()) {
            internalSetProperties(Collections.emptySortedMap());
        }
    }

    @Override
    public Optional<Object> getProperty(String name) {
        return Optional.ofNullable(properties.get(name));
    }

//    @Override
//    public Stream<Entry<String, Object>> stream() {
//        return properties.entrySet().stream();
//    }
//
//    @Override
//    public Stream<Entry<String, Object>> stream(String prefix) {
//        if (!prefix.endsWith(".")) {
//            return stream(prefix + ".");
//        } else {
//            return properties
//                .subMap(prefix, prefix + Character.MAX_VALUE)
//                .entrySet()
//                .stream()
//                .map(PropertySourceUtils.stripPrefix(prefix));
//        }
//    }

    @Override
    public Collection<String> getKeys() {
        return properties.keySet();
    }

    @Override
    public Collection<String> getKeys(String prefix) {
        return properties.subMap(prefix, prefix + Character.MAX_VALUE).keySet();
    }

    @Override
    public AutoCloseable addListener(Consumer<PropertySource> consumer) {
        return listeners.add(consumer, this);
    }
    
    protected void notifyListeners() {
        listeners.forEach(listener -> listener.accept(this));
    }
    
    @Override
    public boolean isEmpty() {
        return properties.isEmpty();
    }

    @Override
    public int size() {
        return properties.size();
    }

    @Override
    public PropertySource snapshot() {
        return new ImmutablePropertySource(name, properties);
    }

    @Override
    public void forEach(BiConsumer<String, Object> consumer) {
        properties.forEach(consumer);
    }
}
