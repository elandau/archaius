package com.netflix.archaius.sources;

import com.netflix.archaius.api.annotations.DefaultValue;
import com.netflix.archaius.api.annotations.PropertyName;
import com.netflix.config.api.PropertySource;
import com.netflix.config.sources.ImmutablePropertySource;
import com.netflix.config.sources.TypeResolvingPropertySource;

import org.junit.Test;

import java.util.List;
import java.util.Map;

public class TypeResolvingPropertySourceTest {
    public static interface Foo {
        String getString();
        
        Integer getInteger();

        @DefaultValue("50")
        Integer getDefaultInteger();
        
        default Boolean getBoolean() { return false; }
        
        @PropertyName(name = "list")
        List<String> getListAbc();
        
        Map<String, Integer> getMap();
    }
    
    @Test
    public void test() {
        System.out.println("Starting");
        PropertySource source = ImmutablePropertySource.builder()
                .put("value", "30")
                .put("foo.string",  "a1")
                .put("foo.integer", "2")
                .put("foo.list", "a,b,c")
//                .put("foo.map", "a=1,b=2,c=3")
                .put("foo.map.a1", "1")
                .put("foo.map.a2", "2")
                .put("foo.map.a3", "${value}")
                .build();
        
        TypeResolvingPropertySource configuration = new TypeResolvingPropertySource(source);
        Foo foo = configuration.get("foo", Foo.class).get();

//        foo.onString((newValue) -> do something);
        
        System.out.println(foo);
    }
    
}
