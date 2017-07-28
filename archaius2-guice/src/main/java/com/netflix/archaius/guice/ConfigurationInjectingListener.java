package com.netflix.archaius.guice;

import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.spi.ProvisionListener;
import com.netflix.archaius.ConfigMapper;
import com.netflix.archaius.LayeredPropertySourceManager;
import com.netflix.archaius.Layers;
import com.netflix.archaius.api.CascadeStrategy;
import com.netflix.archaius.api.PropertyResolver;
import com.netflix.archaius.api.annotations.Configuration;
import com.netflix.archaius.api.annotations.ConfigurationSource;
import com.netflix.archaius.api.exceptions.ConfigException;
import com.netflix.archaius.cascade.NoCascadeStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class ConfigurationInjectingListener implements ProvisionListener {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationInjectingListener.class);
    
    @Inject
    private PropertyResolver  resolver;
    
    @Inject
    private Injector          injector;
    
    @Inject
    private LayeredPropertySourceManager     configManager;
    
    @com.google.inject.Inject(optional = true)
    private CascadeStrategy   cascadeStrategy;
    
    @Inject
    public static void init(ConfigurationInjectingListener listener) {
        LOG.info("Initializing ConfigurationInjectingListener");
    }
    
    CascadeStrategy getCascadeStrategy() {
        return cascadeStrategy != null ? cascadeStrategy : NoCascadeStrategy.INSTANCE;
    }
    
    private ConfigMapper mapper = new ConfigMapper();
    
    @Override
    public <T> void onProvision(ProvisionInvocation<T> provision) {
        Class<?> clazz = provision.getBinding().getKey().getTypeLiteral().getRawType();
        
        //
        // Configuration Loading
        //
        final ConfigurationSource source = clazz.getDeclaredAnnotation(ConfigurationSource.class);
        if (source != null) {
            if (injector == null) {
                LOG.warn("Can't inject configuration into {} until ConfigurationInjectingListener has been initialized", clazz.getName());
                return;
            }
            
            CascadeStrategy strategy = source.cascading() != ConfigurationSource.NullCascadeStrategy.class
                    ? injector.getInstance(source.cascading()) : getCascadeStrategy();

            for (String resourceName : source.value()) {
                LOG.debug("Trying to loading configuration resource {}", resourceName);
                configManager.loadResource(Layers.LIBRARY, loader -> {
                    try {
                        return loader
                                .withCascadeStrategy(strategy)
                                .load(resourceName);
                    } catch (ConfigException e) {
                        throw new ProvisionException("Unable to load configuration for " + resourceName, e);
                    }
                });
            }
        }
        
        //
        // Configuration binding
        //
        Configuration configAnnot = clazz.getAnnotation(Configuration.class);
        if (configAnnot != null) {
            if (injector == null) {
                LOG.warn("Can't inject configuration into {} until ConfigurationInjectingListener has been initialized", clazz.getName());
                return;
            }
            
            try {
                mapper.mapConfig(provision.provision(), resolver, configManager::resolve);
            }
            catch (Exception e) {
                throw new ProvisionException("Unable to bind configuration to " + clazz, e);
            }
        }        
    }
}