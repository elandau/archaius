package com.netflix.config.resolver;

import com.netflix.config.api.PropertySource;
import com.netflix.config.api.TypeResolver;
import com.netflix.config.sources.ImmutablePropertySource;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultPropertyResolverTest {
    private static final TypeResolver.Registry registry = new DefaultTypeResolverRegistry();
    
    @Test
    public void testString() {
        PropertySource source = ImmutablePropertySource.singleton("key", "value");
        DefaultPropertyResolver resolver = new DefaultPropertyResolver(source, registry);
        
        Assert.assertEquals("value", resolver.getString("key").get());
    }
    
    @Test
    public void testIntegerAsString() {
        PropertySource source = ImmutablePropertySource.singleton("key", 1);
        DefaultPropertyResolver resolver = new DefaultPropertyResolver(source, registry);
        
        Assert.assertEquals("1", resolver.getString("key").get());
    }

    @Test
    public void testArrayAsString() {
        PropertySource source = ImmutablePropertySource.singleton("key", Arrays.asList("a", "b"));
        DefaultPropertyResolver resolver = new DefaultPropertyResolver(source, registry);
        
        Assert.assertEquals("[a, b]", resolver.getString("key").get());
    }

    @Test
    public void testInteger() {
        PropertySource source = ImmutablePropertySource.singleton("key", "1");
        DefaultPropertyResolver resolver = new DefaultPropertyResolver(source, registry);
        
        Assert.assertEquals(1, resolver.getInteger("key").get().intValue());
    }
    
    @Test
    public void testIntegerData() {
        PropertySource source = ImmutablePropertySource.singleton("key", 1);
        DefaultPropertyResolver resolver = new DefaultPropertyResolver(source, registry);
        
        Assert.assertEquals(1, resolver.getInteger("key").get().intValue());
    }

    @Test
    public void testBooleanTrue() {
        PropertySource source = ImmutablePropertySource.singleton("key", "true");
        DefaultPropertyResolver resolver = new DefaultPropertyResolver(source, registry);
        
        Assert.assertEquals(true, resolver.getBoolean("key").get().booleanValue());
    }

    @Test
    public void testBooleanFalse() {
        PropertySource source = ImmutablePropertySource.singleton("key", "false");
        DefaultPropertyResolver resolver = new DefaultPropertyResolver(source, registry);
        
        Assert.assertEquals(false, resolver.getBoolean("key").get().booleanValue());
    }

    @Test
    public void testBooleanYes() {
        PropertySource source = ImmutablePropertySource.singleton("key", "yes");
        DefaultPropertyResolver resolver = new DefaultPropertyResolver(source, registry);
        
        Assert.assertEquals(true, resolver.getBoolean("key").get().booleanValue());
    }

    @Test
    public void testBooleanNo() {
        PropertySource source = ImmutablePropertySource.singleton("key", "NO");
        DefaultPropertyResolver resolver = new DefaultPropertyResolver(source, registry);
        
        Assert.assertEquals(false, resolver.getBoolean("key").get().booleanValue());
    }
    
    @Test
    public void testStringMap() {
        PropertySource source = ImmutablePropertySource.singleton("key", "a=b;;c=d;");
        DefaultPropertyResolver resolver = new DefaultPropertyResolver(source, registry);
        
        Map<String, String> expected = new HashMap<>();
        expected.put("a", "b");
        expected.put("c", "d");
        Assert.assertEquals(expected, resolver.getProperty("key", ParameterizedTypes.mapOf(String.class, String.class)).get());
    }

    @Test(expected=RuntimeException.class)
    public void testInvalidStringMap() {
        PropertySource source = ImmutablePropertySource.singleton("key", "a;;");
        DefaultPropertyResolver resolver = new DefaultPropertyResolver(source, registry);
        
        Map<String, String> expected = new HashMap<>();
        expected.put("a", "b");
        expected.put("c", "d");
        Assert.assertEquals(expected, resolver.getProperty("key", ParameterizedTypes.mapOf(String.class, String.class)).get());
    }

    @Test
    public void testIntegerMap() {
        PropertySource source = ImmutablePropertySource.singleton("key", "1=2;;3=4;");
        DefaultPropertyResolver resolver = new DefaultPropertyResolver(source, registry);
        
        Map<Integer, Integer> expected = new HashMap<>();
        expected.put(1, 2);
        expected.put(3, 4);
        Assert.assertEquals(expected, resolver.getProperty("key", ParameterizedTypes.mapOf(Integer.class, Integer.class)).get());
    }

    @Test
    public void testStringSet() {
        PropertySource source = ImmutablePropertySource.singleton("key", "a,b,,c");
        DefaultPropertyResolver resolver = new DefaultPropertyResolver(source, registry);
        
        Set<String> expected = new HashSet<>();
        expected.add("a");
        expected.add("b");
        expected.add("c");
        Assert.assertEquals(expected, resolver.getProperty("key", ParameterizedTypes.setOf(String.class)).get());
    }
    
    @Test
    public void testIntegerSet() {
        PropertySource source = ImmutablePropertySource.singleton("key", "1,2,,3");
        DefaultPropertyResolver resolver = new DefaultPropertyResolver(source, registry);
        
        Set<Integer> expected = new HashSet<>();
        expected.add(1);
        expected.add(2);
        expected.add(3);
        Assert.assertEquals(expected, resolver.getProperty("key", ParameterizedTypes.setOf(Integer.class)).get());
    }

    @Test
    public void testStringList() {
        PropertySource source = ImmutablePropertySource.singleton("key", "a,b,,c");
        DefaultPropertyResolver resolver = new DefaultPropertyResolver(source, registry);
        
        List<String> expected = new ArrayList<>();
        expected.add("a");
        expected.add("b");
        expected.add("c");
        Assert.assertEquals(expected, resolver.getProperty("key", ParameterizedTypes.listOf(String.class)).get());
    }
    
    @Test
    public void testIntegerList() {
        PropertySource source = ImmutablePropertySource.singleton("key", "1,2,,3");
        DefaultPropertyResolver resolver = new DefaultPropertyResolver(source, registry);
        
        List<Integer> expected = new ArrayList<>();
        expected.add(1);
        expected.add(2);
        expected.add(3);
        Assert.assertEquals(expected, resolver.getProperty("key", ParameterizedTypes.listOf(Integer.class)).get());
    }
}
