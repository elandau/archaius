package com.netflix.config.api;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.function.Function;

/**
 * Bundle of a named resource with optional cascading strategy
 */
public final class Bundle {
    public interface CascadeGenerator extends Function<String, List<String>> {
    }
    
    private final String name;
    private final CascadeGenerator cascadeGenerator;
    
    public static Bundle create(String name, CascadeGenerator cascadeGenerator) {
        return new Bundle(name, cascadeGenerator);
    }
    
    private Bundle(String name, CascadeGenerator cascadeGenerator) {
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
