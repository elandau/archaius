package com.netflix.config.sources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import com.netflix.config.api.PropertySource;

/**
 * Immutable property source that is a composite of multiple {@link PropertySource}s.
 * 
 * Each instance maintains a cached version of all of its children.
 */
public class CompositePropertySource extends ImmutablePropertySource {
    private final List<PropertySource> sources;
    
    private static SortedMap<String, Object> joinSources(Collection<PropertySource> sources) {
        SortedMap<String, Object> values = new TreeMap<>();
        sources.forEach(source -> source.forEach((key, value) -> values.putIfAbsent(key, value)));
        return values;
    }
    
    public CompositePropertySource(String name, PropertySource... sources) {
        this(name, Arrays.asList(sources));
    }
    
    public CompositePropertySource(String name, Collection<PropertySource> sources) {
        super(name, joinSources(sources));
        this.sources = Collections.unmodifiableList(new ArrayList<>(sources));
    }
    
    @Override
    public Stream<PropertySource> children() {
        return sources.stream();
    }
}
