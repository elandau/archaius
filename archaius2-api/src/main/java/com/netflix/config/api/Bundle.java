package com.netflix.config.api;

import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Bundle of a named resource with optional cascading strategy
 */
public final class Bundle {
    public interface CascadeGenerator extends Function<String, List<String>> {
    }
    
    public static class EmptyGenerator implements CascadeGenerator {
        @Override
        public List<String> apply(String t) {
            return Collections.singletonList(t);
        }
    }
    
    private final String name;
    private final Class<? extends CascadeGenerator> cascadeGenerator;
    
    public static Bundle named(String name) {
        return new Bundle(name, EmptyGenerator.class);
    }
    
    public Bundle cascadeGenerator(Class<? extends CascadeGenerator> cascadeGenerator) {
        return new Bundle(name, cascadeGenerator);
    }
    
    private Bundle(String name, Class<? extends CascadeGenerator> cascadeGenerator) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(cascadeGenerator);
        this.name = name;
        this.cascadeGenerator = cascadeGenerator;
    }
    
    public Class<? extends CascadeGenerator> getCascadeGenerator() {
        return cascadeGenerator;
    }
    
    public String getName() {
        return name;
    }
}
