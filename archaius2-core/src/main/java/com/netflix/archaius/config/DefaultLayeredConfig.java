package com.netflix.archaius.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.Layer;
import com.netflix.archaius.api.LayeredPropertySource;
import com.netflix.archaius.api.PropertySource;

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
        
        List<PropertySourceHolder> newEntries = new ArrayList<>(state.children);
        newEntries.add(new PropertySourceHolder(layer, position, propertySource));
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
    public void forEachProperty(BiConsumer<String, Object> consumer) {
        this.state.properties.forEach(consumer);
    }

    private static final AtomicInteger insertionOrderCounter = new AtomicInteger(1);
    
    /**
     * Instance of a single child PropertySource within the composite structure
     */
    private static class PropertySourceHolder {
        private final Layer key;
        private final int insertionOrder;
        private final PropertySource propertySource;
        
        private PropertySourceHolder(Layer key, int internalOrder, PropertySource propertySource) {
            this.key = key;
            this.insertionOrder = internalOrder;
            this.propertySource = propertySource;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = 31 + ((propertySource == null) ? 0 : propertySource.hashCode());
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            PropertySourceHolder other = (PropertySourceHolder) obj;
            if (propertySource == null) {
                if (other.propertySource != null)
                    return false;
            } else if (!propertySource.equals(other.propertySource))
                return false;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Element [key=" + key + ", id=" + insertionOrder + ", value=" + propertySource + "]";
        }
    }
    
    private static final Comparator<PropertySourceHolder> ByPriorityAndInsertionOrder = (PropertySourceHolder o1, PropertySourceHolder o2) -> {
        if (o1.key != o2.key) {
            int result = o1.key.getOrder() - o2.key.getOrder();
            if (result != 0) {
                return result;
            }
        }
        
        return o1.key.isReversedOrder()
                ? o1.insertionOrder - o2.insertionOrder
                : o2.insertionOrder - o1.insertionOrder;                        
    };

    /**
     * Immutable composite state of the DefaultLayeredConfig.  A new instance of this
     * will be created whenever a new Config is added or removed
     */
    private class ImmutableCompositeState {
        private final List<PropertySourceHolder> children;
        private final Map<String, Object> properties;
        
        ImmutableCompositeState(List<PropertySourceHolder> entries) {
            this.children = Collections.unmodifiableList(entries);
            this.properties = new HashMap<>();
            this.children.forEach(child -> child.propertySource.forEachProperty(properties::putIfAbsent));
        }        
    }
}
