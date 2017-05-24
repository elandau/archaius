package com.netflix.archaius.sources;

import com.netflix.config.api.Layers;
import com.netflix.config.api.PropertySource;
import com.netflix.config.sources.DefaultSortedCompositePropertySource;
import com.netflix.config.sources.ImmutablePropertySource;
import com.netflix.config.sources.PropertySourceUtils;
import com.netflix.config.sources.SynchronizedMutablePropertySource;

import org.junit.Assert;
import org.junit.Test;

public class LayeredPropertySourceTest {
    private static final PropertySource override = ImmutablePropertySource.builder()
            .named("override")
            .put("foo.bar", "override")
            .build();
    
    private static final PropertySource application = ImmutablePropertySource.builder()
            .named("application")
            .put("foo.bar", "application")
            .build();
    
    private static final PropertySource lib1 = ImmutablePropertySource.builder()
            .named("lib1")
            .put("foo.bar", "lib1")
            .put("default.other", "default")
            .put("default.bar", "default")
            .build();

    private static final PropertySource lib2 = ImmutablePropertySource.builder()
            .named("lib2")
            .put("foo.bar", "lib2")
            .build();
    
    @Test
    public void emptySources() {
        DefaultSortedCompositePropertySource source = new DefaultSortedCompositePropertySource("root");
        
        Assert.assertFalse(source.getProperty("foo").isPresent());
        Assert.assertTrue( source.isEmpty());
        Assert.assertTrue( source.getKeys().isEmpty());
        Assert.assertEquals("root", source.getName());
        
        Assert.assertEquals(0, source.size());
        Assert.assertEquals(0, source.getKeys("prefix").size());
    }
    
    @Test
    public void override() {
        DefaultSortedCompositePropertySource source = new DefaultSortedCompositePropertySource("root");
        
        source.addPropertySource(Layers.LIBRARIES, lib1);
        Assert.assertEquals("lib1", source.getProperty("foo.bar").get());

        source.addPropertySource(Layers.LIBRARIES, lib2);
        Assert.assertEquals("lib2", source.getProperty("foo.bar").get());
        
        source.addPropertySource(Layers.APPLICATION, application);
        Assert.assertEquals("application", source.getProperty("foo.bar").get());
        
        source.addPropertySource(Layers.OVERRIDE, override);
        Assert.assertEquals("override", source.getProperty("foo.bar").get());
    }

    @Test
    public void namespaced() {
        DefaultSortedCompositePropertySource source = new DefaultSortedCompositePropertySource("root");
        source.addPropertySource(Layers.LIBRARIES, lib1);
        source.addPropertySource(Layers.LIBRARIES, lib2);
        source.addPropertySource(Layers.APPLICATION, application);
        source.addPropertySource(Layers.OVERRIDE, override);
    }
    
    @Test
    public void testNotification() {
        SynchronizedMutablePropertySource mutable = new SynchronizedMutablePropertySource("settable");
        
        DefaultSortedCompositePropertySource source = new DefaultSortedCompositePropertySource("test");
        source.addPropertySource(Layers.APPLICATION, application);
        source.addPropertySource(Layers.OVERRIDE, mutable);
        
        Assert.assertEquals("lib1", source.getProperty("foo.bar").get());
        
        source.addListener((s) -> System.out.println("Update"));
        
        mutable.setProperty("foo.bar", "override");
    }
    
    @Test
    public void listSources() {
        DefaultSortedCompositePropertySource source = new DefaultSortedCompositePropertySource("root");
        source.addPropertySource(Layers.LIBRARIES, lib1);
        source.addPropertySource(Layers.LIBRARIES, lib2);
        source.addPropertySource(Layers.APPLICATION, application);
        source.addPropertySource(Layers.OVERRIDE, override);

        PropertySourceUtils.forEachKey(source, "foo.bar", (s, value) -> System.out.println(s.getName() + "=" + value));
    }
}
