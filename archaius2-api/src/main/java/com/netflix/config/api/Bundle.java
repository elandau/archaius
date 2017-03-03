package com.netflix.config.api;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Bundle of a named resource with optional cascading strategy.
 */
public final class Bundle {
    public Bundle(String name, Function<String, List<String>> cascadeGenerator) {
        this.name = name;
        this.cascadeGenerator = (cascadeGenerator != null) ? cascadeGenerator : str -> Collections.singletonList(str);
    }
    
    private final String name;
    private final Function<String, List<String>> cascadeGenerator;
    
    public Function<String, List<String>> getCascadeGenerator() {
        return cascadeGenerator;
    }
    
    public String getName() {
        return name;
    }
}
