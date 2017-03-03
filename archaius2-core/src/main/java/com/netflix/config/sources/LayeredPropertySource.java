package com.netflix.config.sources;

import com.netflix.archaius.internal.WeakReferenceSet;
import com.netflix.config.api.Layer;
import com.netflix.config.api.PropertySource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Composite PropertySource with child sources ordered by {@link Layer}s where there can be 
 * multiple sources in each layer, ordered by insertion order.  Layers form an override 
 * hierarchy for property overrides.  Common hierarchies are, 
 *  Runtime -> Environment -> System -> Application -> Library -> Defaults
 * 
 * TODO: Remove PropertySource
 * TODO: List Layers
 * TODO: Set property?
 * TODO: Get config names
 * TODO: ConfigLoader?
 */
public class LayeredPropertySource extends DelegatingPropertySource {

    private final String name;
    private final WeakReferenceSet<Consumer<PropertySource>> listeners = new WeakReferenceSet<>();
    private final AtomicReference<ImmutableCompositeState> state = new AtomicReference<>(new ImmutableCompositeState(Collections.emptyList()));
    private final ConcurrentMap<Layer, MutablePropertySource> settablePropertySources = new ConcurrentHashMap<>();
    
    public LayeredPropertySource(String name) {
        this.name = name;
    }
    
    @Override
    public String getName() {
        return this.name;
    }

    public void addPropertySourceAtLayer(Layer layer, PropertySource source) {
        addPropertySourceAtLayer(layer, insertionOrderCounter.incrementAndGet(), source);
    }
    
    private void addPropertySourceAtLayer(Layer layer, int layerInternalOrder, PropertySource source) {
        state.getAndUpdate(current -> {
            List<ChildPropertySourceHolder> newEntries = new ArrayList<>(current.children);
            newEntries.add(new ChildPropertySourceHolder(layer, insertionOrderCounter.incrementAndGet(), source));
            newEntries.sort(ByLayerAndInsertionOrder);
            return new ImmutableCompositeState(newEntries);
        });
        
        source.addListener(this::notifyListeners);
    }

    private MutablePropertySource getOrCreateMutablePropertySource(Layer layer) {
        return settablePropertySources.computeIfAbsent(layer, l -> { 
                    MutablePropertySource source = new MutablePropertySource("settable");
                    addPropertySourceAtLayer(layer, 0, source);
                    return source;
                });
    }
    
    /**
     * Set a property for the layer.  This value takes precedence over any others in the layer.
     */
    public void setPropertyAtLayer(Layer layer, String key, Object value) {
        getOrCreateMutablePropertySource(layer).setProperty(key, value);
    }

    /**
     * Clear a property from the override the specified layer.  Once cleared 
     * 
     * @param layer
     * @param key
     */
    public void clearPropertyAtLayer(Layer layer, String key) {
        getOrCreateMutablePropertySource(layer).clearProperty(key);
    }

    @Override
    public AutoCloseable addListener(Consumer<PropertySource> consumer) {
        return listeners.add(consumer, this);
    }
    
    private void notifyListeners(PropertySource updatedPropertySource) {
        listeners.forEach(listener -> listener.accept(this));
    }

    @Override
    protected PropertySource delegate() {
        return state.get().data;
    }
    
    private static final AtomicInteger insertionOrderCounter = new AtomicInteger(1);
    
    /**
     * Instance of a single child PropertySource within the composite structure
     */
    private static class ChildPropertySourceHolder {
        private final Layer layer;
        private final int insertionOrder;
        private final PropertySource source;
        
        private ChildPropertySourceHolder(Layer layer, int internalOrder, PropertySource source) {
            this.layer = layer;
            this.insertionOrder = internalOrder;
            this.source = source;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = 31 + ((source == null) ? 0 : source.hashCode());
            result = prime * result + ((layer == null) ? 0 : layer.hashCode());
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
            ChildPropertySourceHolder other = (ChildPropertySourceHolder) obj;
            if (source == null) {
                if (other.source != null)
                    return false;
            } else if (!source.equals(other.source))
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
            return "Element [layer=" + layer + ", id=" + insertionOrder + ", value=" + source + "]";
        }
    }
    
    private static final Comparator<ChildPropertySourceHolder> ByLayerAndInsertionOrder = (ChildPropertySourceHolder o1, ChildPropertySourceHolder o2) -> {
        if (o1.layer != o2.layer) {
            int result = o1.layer.getOrder() - o2.layer.getOrder();
            if (result != 0) {
                return result;
            }
        }
        
        return o2.insertionOrder - o1.insertionOrder;
    };

    /**
     * Immutable composite state of the LayeredPropertySource.  A new instance of this
     * will be created whenever a new PropertySource is added, removed or updated
     */
    private class ImmutableCompositeState {
        private final List<ChildPropertySourceHolder> children;
        private final PropertySource data;
        
        ImmutableCompositeState(List<ChildPropertySourceHolder> entries) {
            SortedMap<String, Object> map = new TreeMap<>();
            
            this.children = Collections.unmodifiableList(entries);
            this.children.forEach(element -> element.source.forEach((key, value) -> map.putIfAbsent(key, value)));
            
            this.data = new ImmutablePropertySource(name, map);
        }        
    }

    /**
     * Examples
     * 
     * <li>Child names</li>
     * {@code 
     * source.flatten().map(Source::getName).collect(Collector.toList());
     * }
     * 
     */
    @Override
    public Stream<PropertySource> children() {
        return state.get().children.stream().map(element -> element.source);
    }
}
