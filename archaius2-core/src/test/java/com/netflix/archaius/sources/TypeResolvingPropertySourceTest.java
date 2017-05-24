package com.netflix.archaius.sources;

import com.netflix.archaius.api.annotations.DefaultValue;
import com.netflix.archaius.api.annotations.PropertyName;
import com.netflix.config.api.PropertySource;
import com.netflix.config.resolver.DefaultPropertyResolver;
import com.netflix.config.resolver.DefaultTypeResolverRegistry;
import com.netflix.config.sources.ImmutablePropertySource;

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
        
        DefaultPropertyResolver configuration = new DefaultPropertyResolver(source, new DefaultTypeResolverRegistry());
        Foo foo = configuration.getProperty("foo", Foo.class).get();

        System.out.println(foo);
    }
    
    @Test
    public void testInterpolatingSubset() {
        PropertySource source = ImmutablePropertySource.builder()
                .put("value", "30")
                .put("foo.string",  "${value}")
                .build();
//        
//        DefaultResolvingPropertySource configuration = new DefaultResolvingPropertySource(source);
//        System.out.println(configuration.getKeys());
//        
//        Assert.assertEquals("30", configuration.getString("foo.string").orElse(""));
//        DefaultResolvingPropertySource prefixed = configuration.subset("foo");
//        Assert.assertEquals("30", prefixed.getString("string").orElse(""));
//        
//        System.out.println(configuration.getKeys());
//        System.out.println(prefixed.getKeys());
    }
    
}
