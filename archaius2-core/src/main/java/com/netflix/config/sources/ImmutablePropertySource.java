package com.netflix.config.sources;

import com.netflix.archaius.internal.Preconditions;
import com.netflix.config.api.PropertySource;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * Immutable PropertySource with a builder for convenient creation. Most files are loaded
 * into an instance of ImmutablePropertySource.
 */
public class ImmutablePropertySource implements PropertySource {
    protected SortedMap<String, Object> properties;
    protected String name;
    protected volatile Integer hashCode = null;
    private static final AtomicInteger counter = new AtomicInteger();

    /**
     * The builder only provides convenience for fluent style adding of properties
     * 
     * {@code
     * <pre>
     * ImmutablePropertySource.builder()
     *      .put("foo", "bar")
     *      .put("baz", 123)
     *      .build()
     * </pre>
     * }
     */
    public static class Builder {
        ImmutablePropertySource source = new ImmutablePropertySource("", new TreeMap<>());
        
        public Builder named(String name) {
            Preconditions.checkArgument(source != null, "Builder already created");
            source.name = name;
            return this;
        }
        
        public Builder putSource(PropertySource propertySource) {
            Preconditions.checkArgument(source != null, "Builder already created");
            propertySource.forEach((key, value) -> source.properties.putIfAbsent(key, value));
            return this;
        }
        
        public Builder putSources(Collection<PropertySource> sources) {
            Preconditions.checkArgument(source != null, "Builder already created");
            sources.forEach(this::putSource);
            return this;
        }
        
        public Builder put(String key, Object value) {
            Preconditions.checkArgument(source != null, "Builder already created");
            source.properties.put(key, value);
            return this;
        }
        
        public Builder putIfAbsent(String key, Object value) {
            Preconditions.checkArgument(source != null, "Builder already created");
            source.properties.putIfAbsent(key, value);
            return null;
        }

        public Builder putAll(Map<String, ?> values) {
            Preconditions.checkArgument(source != null, "Builder already created");
            values.forEach((k, v) -> source.properties.put(k, v));
            return this;
        }
        
        public Builder putAll(Properties props) {
            props.forEach((key, value) -> source.properties.put(key.toString(), value));
            return this;
        }
        
        public ImmutablePropertySource build() {
            try {
                if (source.name == null) {
                    source.name = "map-" + counter.incrementAndGet();
                }
                return source;
            } finally {
                source = null;
            }
        }
    }
    
    public static ImmutablePropertySource singleton(String key, Object value) {
        return builder().put(key, value).build();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    ImmutablePropertySource(String name, SortedMap<String, Object> properties) {
        this.name = name;
        this.properties = properties;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<Object> getProperty(String name) {
        return Optional.ofNullable(properties.get(name));
    }

    @Override
    public Collection<String> getKeys() {
        return properties.keySet();
    }

    @Override
    public Collection<String> getKeys(String prefix) {
        return properties.subMap(prefix, prefix + Character.MAX_VALUE).keySet();
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
        return this;
    }

    @Override
    public void forEach(BiConsumer<String, Object> consumer) {
        properties.forEach(consumer);
    }
    
    @Override
    public int hashCode() {
        if (hashCode == null) {
            hashCode = properties.hashCode();
        }
        return hashCode;
    }

    @Override
    public PropertySource subset(String prefix) {
        if (prefix.isEmpty()) {
            return this;
        } else if (!prefix.endsWith(".")) {
            return subset(prefix + ".");
        } else {
            return new SubsetPropertySource(this, prefix);
        }
    }

    @Override
    public void forEach(String prefix, BiConsumer<String, Object> consumer) {
        properties.subMap(prefix, prefix + Character.MAX_VALUE).forEach(consumer);
    }

    @Override
    public String toString() {
        return "ImmutablePropertySource [name=" + name + "]";
    }
}
