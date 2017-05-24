package com.netflix.archaius.guice;

import org.junit.Test;

import com.google.inject.Guice;

public class InternalArchaiusModuleTest {

    @Test
    public void succeedOnDuplicateInstall() {
        Guice.createInjector(
                new ArchaiusModule(),
                new ArchaiusModule());
    }
}
