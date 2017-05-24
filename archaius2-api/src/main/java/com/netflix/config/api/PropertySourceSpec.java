package com.netflix.config.api;

import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Bundle of a named resource with optional cascading strategy
 */
public final class PropertySourceSpec {
    public interface CascadeGenerator extends Function<String, List<String>> {
    }
    
    public static CascadeGenerator NO_CASCADE = Collections::singletonList;
    
    private final String name;
    private final CascadeGenerator cascadeGenerator;
    
    public static PropertySourceSpec create(String name) {
        return create(name, NO_CASCADE);
    }
    
    public static PropertySourceSpec create(String name, CascadeGenerator cascadeGenerator) {
        return new PropertySourceSpec(name, cascadeGenerator);
    }
    
    private PropertySourceSpec(String name, CascadeGenerator cascadeGenerator) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(cascadeGenerator);
        this.name = name;
        this.cascadeGenerator = cascadeGenerator;
    }
    
    public CascadeGenerator getCascadeGenerator() {
        return cascadeGenerator;
    }
    
    public String getName() {
        return name;
    }
}
