package com.netflix.archaius.config;

import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.Layer;
import com.netflix.archaius.api.LayeredPropertySource;
import com.netflix.archaius.api.PropertySource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Composite Config with child sources ordered by {@link Layer}s where there can be 
 * multiple configs in each layer, ordered by insertion order.  Layers form an override 
 * hierarchy for property overrides.  Common hierarchies are, 
 * 
 *  Runtime -> Environment -> System -> Application -> Library -> Defaults
 */
public class DefaultLayeredConfig extends AbstractConfig implements LayeredPropertySource {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultLayeredConfig.class);
    
    private final String name;
    private volatile ImmutableCompositeState state = new ImmutableCompositeState(Collections.emptyList());
    
    public DefaultLayeredConfig(String name) {
        this.name = name;
    }
    
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getRawProperty(String key) {
        return state.properties.get(key);
    }

    @Override
    public boolean containsKey(String key) {
        return state.properties.containsKey(key);
    }

    @Override
    public boolean isEmpty() {
        return state.properties.isEmpty();
    }

    @Override
    public Iterator<String> getKeys() {
        return state.properties.keySet().iterator();
    }

    @Override
    public synchronized void addPropertySource(Layer layer, PropertySource propertySource) {
        addPropertySource(layer, propertySource, insertionOrderCounter.incrementAndGet());
    }
    
    public synchronized void addPropertySource(Layer layer, PropertySource propertySource, int position) {
        LOG.info("Adding property source {} at layer {}", propertySource.getName(), layer);
        
        List<LayerAndPropertySource> newEntries = new ArrayList<>(state.children);
        newEntries.add(new LayerAndPropertySource(layer, propertySource, position));
        newEntries.sort(ByPriorityAndInsertionOrder);
        state = new ImmutableCompositeState(newEntries);
        
        Config child = (Config)propertySource;
        child.setStrInterpolator(getStrInterpolator());
        child.setDecoder(getDecoder());
        notifyConfigUpdated(child);
        child.addListener(this::notifyConfigUpdated);

        this.notifyConfigUpdated(this);
    }

    @Override
    public Collection<PropertySource> getPropertySourcesAtLayer(Layer layer) {
        return state.children.stream()
                .filter(holder -> holder.layer.equals(layer))
                .map(holder -> holder.propertySource)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<PropertySource> getPropertySources() {
        return this.state.children.stream().map(LayerAndPropertySource::getPropertySource).collect(Collectors.toList());
    }

    @Override
    public synchronized Optional<PropertySource> removePropertySource(Layer layer, String name) {
        LOG.info("Removing property source {} at layer {}", name, layer);
        List<LayerAndPropertySource> newEntries = new ArrayList<>(state.children);
        Optional<PropertySource> previous = newEntries
                .stream()
                .filter(source -> source.layer.equals(layer) && source.propertySource.getName().equals(name))
                .map(LayerAndPropertySource::getPropertySource)
                .findFirst();
                
        newEntries.sort(ByPriorityAndInsertionOrder);
        state = new ImmutableCompositeState(newEntries);
        this.notifyConfigUpdated(this);
        
        return previous;
    }

    @Override
    public void forEachProperty(BiConsumer<String, Object> consumer) {
        this.state.properties.forEach(consumer);
    }

    private static final AtomicInteger insertionOrderCounter = new AtomicInteger(1);
    
    /**
     * Instance of a single child PropertySource within the composite structure
     */
    private static class LayerAndPropertySource {
        private final Layer layer;
        private final int internalOrder;
        private final PropertySource propertySource;
        
        private LayerAndPropertySource(Layer layer, PropertySource propertySource, int internalOrder) {
            this.layer = layer;
            this.internalOrder = internalOrder;
            this.propertySource = propertySource;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = 31 + ((propertySource == null) ? 0 : propertySource.hashCode());
            result = prime * result + ((layer == null) ? 0 : layer.hashCode());
            return result;
        }

        public Layer getLayer() {
            return layer;
        }
        
        public PropertySource getPropertySource() {
            return propertySource;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            LayerAndPropertySource other = (LayerAndPropertySource) obj;
            if (propertySource == null) {
                if (other.propertySource != null)
                    return false;
            } else if (!propertySource.equals(other.propertySource))
                return false;
            if (layer == null) {
                if (other.layer != null)
                    return false;
            } else if (!layer.equals(other.layer))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Element [layer=" + layer + ", id=" + internalOrder + ", value=" + propertySource + "]";
        }
    }
    
    private static final Comparator<LayerAndPropertySource> ByPriorityAndInsertionOrder = (LayerAndPropertySource o1, LayerAndPropertySource o2) -> {
        if (o1.layer != o2.layer) {
            int result = o1.layer.getOrder() - o2.layer.getOrder();
            if (result != 0) {
                return result;
            }
        }
        
        return o1.layer.isReversedOrder()
                ? o1.internalOrder - o2.internalOrder
                : o2.internalOrder - o1.internalOrder;                        
    };

    /**
     * Immutable composite state of the DefaultLayeredConfig.  A new instance of this
     * will be created whenever a new Config is added or removed
     */
    private class ImmutableCompositeState {
        private final List<LayerAndPropertySource> children;
        private final Map<String, Object> properties;
        
        ImmutableCompositeState(List<LayerAndPropertySource> entries) {
            this.children = Collections.unmodifiableList(entries);
            this.properties = new HashMap<>();
            this.children.forEach(child -> child.propertySource.forEachProperty(properties::putIfAbsent));
        }        
    }
}
