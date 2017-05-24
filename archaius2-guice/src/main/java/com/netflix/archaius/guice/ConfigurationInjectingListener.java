package com.netflix.archaius.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.ProvisionListener;
import com.netflix.archaius.ConfigMapper;
import com.netflix.archaius.api.annotations.ConfigurationSource;
import com.netflix.config.api.PropertySourceSpec;
import com.netflix.config.api.PropertySourceSpec.CascadeGenerator;
import com.netflix.config.api.Configuration;
import com.netflix.config.api.Layers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ConfigurationInjectingListener implements ProvisionListener {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationInjectingListener.class);
    
    @Inject
    private Injector injector;
    
    private Configuration configuration;
    private CascadeGenerator cascadeGenerator;
    
    private ConfigMapper mapper = new ConfigMapper();
    
    @Inject
    private void setLayeredPropertySource(Configuration configuration, CascadeGenerator cascadeGenerator) {
        this.configuration = configuration;
        this.cascadeGenerator = cascadeGenerator;
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
                
                
                PropertySourceSpec bundle = PropertySourceSpec.create(
                        bundleName, 
                        Optional
                            .ofNullable(source.cascading())
                            .map(injector::getInstance)
                            .map(strategy -> strategy.generate(bundleName, lookup -> str -> str, str -> str))
                            .map(new Function<List<String>, CascadeGenerator>() {
                                @Override
                                public CascadeGenerator apply(List<String> list) {
                                    return str -> list;
                                }
                            }).orElse(null));
                
                configuration.addPropertySourceSpec(Layers.LIBRARIES, bundle);
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