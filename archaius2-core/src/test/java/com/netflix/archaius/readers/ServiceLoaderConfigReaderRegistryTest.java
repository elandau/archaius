package com.netflix.archaius.readers;

import org.junit.Assert;
import org.junit.Test;

public class ServiceLoaderConfigReaderRegistryTest {
    @Test
    public void testCoreConfigReaders() {
        ServiceLoaderConfigReaderRegistry registry = new ServiceLoaderConfigReaderRegistry();
        Assert.assertEquals(1, registry.getReaders().size());
        Assert.assertEquals(PropertiesConfigReader.class, registry.getReaders().iterator().next().getClass());
    }
}
