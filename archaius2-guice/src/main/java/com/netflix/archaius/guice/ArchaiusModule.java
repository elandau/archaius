/**
 * Copyright 2015 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.archaius.guice;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.netflix.archaius.api.CascadeStrategy;
import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.inject.DefaultLayer;
import com.netflix.archaius.api.inject.RemoteLayer;
import com.netflix.archaius.config.ConfigToPropertySource;
import com.netflix.config.api.Layer;
import com.netflix.config.api.Layers;
import com.netflix.config.api.PropertySource;
import com.netflix.config.sources.ImmutablePropertySource;
import com.netflix.config.sources.LayeredPropertySource;
import com.netflix.governator.providers.Advises;

/**
 * Guice Module for bootstrapping archaius's {@link DefaultConfigManager} and making its components injectable. 
 * 
 * ArchaiusModule exposes several mechanisms to reduce boilderplate while allowing for customization of 
 * the ConfigManager. 
 * 
 * First, its possible to customize basic features by calling methods of the ArchaiusModule, which are 
 * chainable.
 * 
 * For example, to change the main application layer configuration resources name from the default 
 * 'application' to 'myapplication', 
 * {@code
    Guice.createInjector(new ArchaiusModule()
        .withConfigName("myapplication"));
 * }
 * 
 * It's also possible to customize all methods of the {@link DefaultConfigManager#builder()} by passing a consumer
 * to {@link ArchaiusModule#configure(Consumer)}.  Note that field injection is allowed on the consumer.
 * 
 * For example, to add a test override layer to configuration, 
 * {@code
    Guice.createInjector(new ArchaiusModule().configure(builder ->
        builder.addConfigToLayer(Layers.TEST, overrideProperties)));
 * }
 * 
 * The builder can be further customized using {@link @Advises} like so,
 * {@code
    Guice.createInjector(new ArchaiusModule() {
        {@literal @}Advises(order=Integer.MAX_VALUE)
        {@literal @}Singleton
        UnaryOperator<DefaultConfigManager.Builder> adviseConfigManagerBuilder() {
            return builder -> builder.withConfigName("myapplication");
        }    
    });
 * }
 * 
 * Note that while this method is more verbose it does allow for more deterministic control over the order in which 
 * customizations are applied to the builder (driven by {@link Advises#order()}).  Furthermore injections
 * into the @Advises method is much more straightforward (no need for field injection).
 */
public class ArchaiusModule extends AbstractModule {
    public static final int DEFAULT = 0;
    
    private Class<? extends CascadeStrategy> cascadeStrategy = null;
    private String configName = null;
    private final List<Consumer<LayeredPropertySource>> overrides = new ArrayList<>();
    
    public ArchaiusModule withCascadeStrategy(Class<? extends CascadeStrategy> cascadeStrategy) {
        this.cascadeStrategy = cascadeStrategy;
        return this;
    }

    public ArchaiusModule withConfigName(String configName) {
        this.configName = configName;
        return this;
    }
    
    public ArchaiusModule withPropertySource(Layer layer, PropertySource source) {
        overrides.add((root) -> root.addPropertySourceAtLayer(layer, source));
        return this;
    }
    
    public ArchaiusModule withApplicationOverrides(Properties props) {
        return withPropertySource(
                Layers.APPLICATION_OVERRIDE, 
                ImmutablePropertySource.builder().putAll(props).build());
    }
    
    public ArchaiusModule configure(Consumer<LayeredPropertySource> consumer) {
        overrides.add(consumer);
        return this;
    }
    
    @Deprecated
    public ArchaiusModule withApplicationOverrides(Config config) {
        return withPropertySource(
                Layers.APPLICATION_OVERRIDE, 
                new ConfigToPropertySource("", config));
    }
    
    /**
     * Customize the filename for the main application configuration.  The default filename is
     * 'application'.  
     * 
     * <code>
     * install(new ArchaiusModule() {
     *    {@literal @}Override
     *    protected void configureArchaius() {
     *        bindConfigurationName().toInstance("myconfig");
     *    }
     * });
     * </code>
     * 
     * @return LinkedBindingBuilder to which the implementation is set
     */
    @Deprecated
    protected LinkedBindingBuilder<String> bindConfigurationName() {
        return bind(String.class).annotatedWith(Names.named(LegacyInternalArchaiusModule.CONFIG_NAME_KEY));
    }
    
    /**
     * Set application overrides.  This is normally done for unit tests.
     * 
     * <code>
     * install(new ArchaiusModule() {
     *    {@literal @}Override
     *    protected void configureArchaius() {
     *        bindApplicationConfigurationOverride().toInstance(MapConfig.builder()
     *          .put("some_property_to_override", "value")
     *          .build()
     *          );
     *    }
     * });
     * </code>
     * 
     * @return LinkedBindingBuilder to which the implementation is set
     */
    @Deprecated
    protected LinkedBindingBuilder<Config> bindApplicationConfigurationOverride() {
        return bind(Config.class).annotatedWith(ApplicationOverride.class);
    }
    
    /**
     * Specify the Config to use for the remote layer. 
     * 
     * <code>
     * install(new ArchaiusModule() {
     *    {@literal @}Override
     *    protected void configureArchaius() {
     *        bindRemoteConfig().to(SomeRemoteConfigImpl.class);
     *    }
     * });
     * </code>
     * 
     * @return LinkedBindingBuilder to which the implementation is set
     * @deprecated See {@link ArchaiusModule#configure(Consumer)}
     */
    @Deprecated
    protected LinkedBindingBuilder<Config> bindRemoteConfig() {
        return bind(Config.class).annotatedWith(RemoteLayer.class);
    }
    
    /**
     * Specify the CascadeStrategy used to load environment overrides for application and
     * library configurations.
     * 
     * <code>
     * install(new ArchaiusModule() {
     *    {@literal @}Override
     *    protected void configureArchaius() {
     *        bindCascadeStrategy().to(MyCascadeStrategy.class);
     *    }
     * });
     * </code>
     * 
     * @return LinkedBindingBuilder to which the implementation is set
     * @deprecated See {@link ArchaiusModule#configure(Consumer)}
     */
    @Deprecated
    protected LinkedBindingBuilder<CascadeStrategy> bindCascadeStrategy() {
        return bind(CascadeStrategy.class);
    }
    
    /**
     * Add a config to the bottom of the Config hierarchy.  Use this when configuration is added
     * through code.  Can be called multiple times as ConfigReader is added to a multibinding.
     * 
     * <code>
     * install(new ArchaiusModule() {
     *    {@literal @}Override
     *    protected void configureArchaius() {
     *        bindDefaultConfig().to(MyDefaultConfig.class);
     *    }
     * });
     * </code>
     * 
     * @return LinkedBindingBuilder to which the implementation is set
     * @deprecated See {@link ArchaiusModule#configure(Consumer)}
     */
    @Deprecated
    protected LinkedBindingBuilder<Config> bindDefaultConfig() {
        return Multibinder.newSetBinder(binder(), Config.class, DefaultLayer.class).addBinding();
    }

    /**
     * Add support for a new configuration format.  Can be called multiple times to add support for
     * multiple file format.
     * 
     * <code>
     * install(new ArchaiusModule() {
     *    {@literal @}Override
     *    protected void configureArchaius() {
     *        bindConfigReader().to(SomeConfigFormatReader.class);
     *    }
     * });
     * </code>
     * 
     * @return LinkedBindingBuilder to which the implementation is set
     * @deprecated See {@link ArchaiusModule#configure(Consumer)}
     */
    @Deprecated
    protected LinkedBindingBuilder<Config> bindConfigReader() {
        return Multibinder.newSetBinder(binder(), Config.class, DefaultLayer.class).addBinding();
    }

    /**
     * Set application overrides to a particular resource.  This is normally done for unit tests.
     *
     * <code>
     * install(new ArchaiusModule() {
     *    {@literal @}Override
     *    protected void configureArchaius() {
     *        bindApplicationConfigurationOverrideResource("laptop");
     *    }
     * });
     * </code>
     *
     * @deprecated See {@link ArchaiusModule#configure(Consumer)}
     */
    @Deprecated
    protected void bindApplicationConfigurationOverrideResource(String overrideResource)  {
        Multibinder.newSetBinder(binder(), String.class, ApplicationOverrideResources.class).permitDuplicates().addBinding().toInstance(overrideResource);
    }

    @Deprecated
    protected void configureArchaius() {
    }

    @Override
    protected final void configure() {
        install(new InternalArchaiusModule());
        install(new LegacyInternalArchaiusModule());

        configureArchaius();

        if (cascadeStrategy != null) {
            this.bindCascadeStrategy().to(cascadeStrategy);
        }
    }
    
    @Advises
    @Singleton
    UnaryOperator<LayeredPropertySource> applyConsumers(Injector injector) throws Exception {
        return source -> {
            overrides.forEach(override -> override.accept(source));
            return source;
        };
    }    
}
