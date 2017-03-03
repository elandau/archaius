package com.netflix.archaius.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.ProvisionListener;
import com.netflix.archaius.ConfigMapper;
import com.netflix.archaius.api.CascadeStrategy;
import com.netflix.archaius.api.StrInterpolator;
import com.netflix.archaius.api.annotations.ConfigurationSource;
import com.netflix.archaius.cascade.NoCascadeStrategy;
import com.netflix.archaius.interpolate.CommonsStrInterpolator;
import com.netflix.config.api.Bundle;
import com.netflix.config.api.Layers;
import com.netflix.config.api.PropertySource;
import com.netflix.config.sources.LayeredPropertySource;
import com.netflix.config.sources.formats.PropertySourceFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ConfigurationInjectingListener implements ProvisionListener {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationInjectingListener.class);
    
    @Inject
    private Injector injector;
    
    @com.google.inject.Inject(optional=true)
    private CascadeStrategy cascadeStrategy = new NoCascadeStrategy();
    
    private StrInterpolator.Lookup lookup;
    private StrInterpolator interpolator;
    
    private LayeredPropertySource mainSource;
    private PropertySourceFactory factory;
    private ConfigMapper mapper = new ConfigMapper();
    
    @Inject
    private void setLayeredPropertySource(LayeredPropertySource mainSource) {
        this.mainSource = mainSource;
        
        this.factory = new PropertySourceFactory(mainSource);
        
        StrInterpolator.Lookup lookup = key -> mainSource.getProperty(key).map(Object::toString).orElse(null);
        this.interpolator = value -> CommonsStrInterpolator.INSTANCE.create(lookup);
    }    
    
    @Inject
    public static void init(ConfigurationInjectingListener listener) {
        LOG.info("Initializing ConfigurationInjectingListener");
    }
    
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
            
            Arrays.asList(source.value()).forEach(bundleName -> {
                LOG.debug("Trying to loading configuration bundle {}", bundleName);
                
                Bundle bundle = new Bundle(bundleName, (name) -> {
                    CascadeStrategy strategy = source.cascading() != ConfigurationSource.NullCascadeStrategy.class
                        ? injector.getInstance(source.cascading())
                        : cascadeStrategy;
                    return strategy.generate(bundleName, interpolator, lookup);
                });
                
                PropertySource loadedPropertySource = factory.apply(bundle);
                mainSource.addPropertySourceAtLayer(Layers.LIBRARIES, loadedPropertySource);
            });
        }
        
        //
        // Configuration binding
        //
//        Configuration configAnnot = clazz.getAnnotation(Configuration.class);
//        if (configAnnot != null) {
//            if (injector == null) {
//                LOG.warn("Can't inject configuration into {} until ConfigurationInjectingListener has been initialized", clazz.getName());
//                return;
//            }
//            
//            try {
//                mapper.mapConfig(provision.provision(), config, new IoCContainer() {
//                    @Override
//                    public <S> S getInstance(String name, Class<S> type) {
//                        return injector.getInstance(Key.get(type, Names.named(name)));
//                    }
//                });
//            }
//            catch (Exception e) {
//                throw new ProvisionException("Unable to bind configuration to " + clazz, e);
//            }
//        }        
    }
    
    public static Module asModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                ConfigurationInjectingListener listener = new ConfigurationInjectingListener();
                requestInjection(listener);
                bind(ConfigurationInjectingListener.class).toInstance(listener);
                requestStaticInjection(ConfigurationInjectingListener.class);
                bindListener(Matchers.any(), listener);
            }
            
            @Override
            public int hashCode() {
                return getClass().hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                return obj != null && getClass().equals(obj.getClass());
            }
        };
    }
}