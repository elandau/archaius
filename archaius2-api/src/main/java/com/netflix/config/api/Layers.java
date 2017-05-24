package com.netflix.config.api;

/**
 * Set of pre-defined configuration override layers that should be realistic for most applications
 * and tests
 */
public final class Layers {
    /**
     * Layer for test specific configurations.  This layer should only be installed for unit
     * tests and will take precedence over any other layer
     */
    public static final Layer TEST                  = Layer.of("test",        100, false);
    
    /**
     * Layer reserved for code override and is normally attached to a settable config
     */
    public static final Layer OVERRIDE              = Layer.of("override",    200, false);

    /**
     * Layer with immutable system properties (-D)
     */
    public static final Layer SYSTEM                = Layer.of("sys",         300, false);
    
    /**
     * Layer with immutable environment properties
     */
    public static final Layer ENVIRONMENT           = Layer.of("env",         400, false);
    
    /**
     * Layer reserved for remove configuration overrides from persistent storage
     */
    public static final Layer REMOTE_OVERRIDE       = Layer.of("remote",      500, false);
    
    /**
     * Override for application configuration loaded in property files.
     */
    public static final Layer APPLICATION_OVERRIDE  = Layer.of("app_override",600, false);

    /**
     * Layer to be used by the application for application specific configurations
     * and allows for the application to override any configuration loaded by libraries
     */
    public static final Layer APPLICATION           = Layer.of("app",         700, false);
    
    /**
     * Layer into which any class or 'library' may load its configuration
     */
    public static final Layer LIBRARIES             = Layer.of("libraries",   800, false);
    
    /**
     * Layer for programmatic defaults 
     */
    public static final Layer DEFAULTS              = Layer.of("defaults",    900, false);
}
