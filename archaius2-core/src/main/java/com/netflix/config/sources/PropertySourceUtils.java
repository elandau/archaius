package com.netflix.config.sources;

import com.netflix.config.api.CompositePropertySource;
import com.netflix.config.api.PropertySource;

import java.util.function.BiConsumer;

public abstract class PropertySourceUtils {
    private PropertySourceUtils() {}
    
    public static void forEachKey(CompositePropertySource root, String key, BiConsumer<PropertySource, Object> consumer) {
        root.forEach(source -> {
            if (source instanceof CompositePropertySource) {
                forEachKey((CompositePropertySource)source, key, consumer);
            } else {
                source.getProperty(key).ifPresent(value -> consumer.accept(source, value));
            }
        });
    }
    
//    int indent = -1;
//    
//    public PropertySourceUtils() {
//        
//    }
//    
//    public void onPropertySource(PropertySource source) {
//        System.out.println(StringUtils.repeat(" ", indent) + source.getName());
//        indent++;
//        source.forEach((key, value) -> onProperty(key, value.toString()));
////        source.children().forEach(s -> onPropertySource(s));
//        indent--;
//    }
//
//    public void onProperty(String key, String value) {
//        System.out.println(StringUtils.repeat(" ", indent) + key + "=" + value);
//    }
//    
//    public void finish(PropertySourceUtils printer) {
//    }
    
}
