package com.netflix.config.sources;

import com.netflix.archaius.internal.WeakReferenceSet;
import com.netflix.config.api.PropertySource;
import com.netflix.config.api.SortedCompositePropertySource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

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
public class DefaultSortedCompositePropertySource extends DelegatingPropertySource implements SortedCompositePropertySource {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultSortedCompositePropertySource.class);
    
    private final String name;
    private final WeakReferenceSet<Consumer<PropertySource>> listeners = new WeakReferenceSet<>();
    private final AtomicReference<ImmutableCompositeState> state = new AtomicReference<>(new ImmutableCompositeState(Collections.emptyList()));
    
    public DefaultSortedCompositePropertySource(String name) {
        this.name = name;
    }
    
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Add a property source to the end of the specified layer (or beginning if Layer.reversed() == true)
     * 
     * @param layer Key identifier
     * @param source
     */
    @Override
    public DefaultSortedCompositePropertySource addPropertySource(Layer layer, PropertySource source) {
        addPropertySourceAtLayer(layer, insertionOrderCounter.incrementAndGet(), source);
        return this;
    }
    
    @Override
    public DefaultSortedCompositePropertySource addPropertySource(Layer layer,
            Function<SortedCompositePropertySource, PropertySource> loader) {
        return addPropertySource(layer, loader.apply(this));
    }

    @Override
    public void forEach(Consumer<PropertySource> consumer) {
        this.state.get().children.stream().map(holder -> holder.source).forEach(consumer);
    }

    private void addPropertySourceAtLayer(Layer key, int keyInternalOrder, PropertySource source) {
        state.getAndUpdate(current -> {
            List<ChildPropertySourceHolder> newEntries = new ArrayList<>(current.children);
            newEntries.add(new ChildPropertySourceHolder(key, insertionOrderCounter.incrementAndGet(), source));
            newEntries.sort(ByPriorityAndInsertionOrder);
            return new ImmutableCompositeState(newEntries);
        });
        
        source.addListener(this::notifyListeners);
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
        private final Layer key;
        private final int insertionOrder;
        private final PropertySource source;
        
        private ChildPropertySourceHolder(Layer key, int internalOrder, PropertySource source) {
            this.key = key;
            this.insertionOrder = internalOrder;
            this.source = source;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = 31 + ((source == null) ? 0 : source.hashCode());
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
            ChildPropertySourceHolder other = (ChildPropertySourceHolder) obj;
            if (source == null) {
                if (other.source != null)
                    return false;
            } else if (!source.equals(other.source))
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
            return "Element [key=" + key + ", id=" + insertionOrder + ", value=" + source + "]";
        }
    }
    
    private static final Comparator<ChildPropertySourceHolder> ByPriorityAndInsertionOrder = (ChildPropertySourceHolder o1, ChildPropertySourceHolder o2) -> {
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
     * Immutable composite state of the LayeredPropertySource.  A new instance of this
     * will be created whenever a new PropertySource is added, removed or updated
     */
    private class ImmutableCompositeState {
        private final List<ChildPropertySourceHolder> children;
        private final PropertySource data;
        
        ImmutableCompositeState(List<ChildPropertySourceHolder> entries) {
            SortedMap<String, Object> map = new TreeMap<>();
            
            this.children = Collections.unmodifiableList(entries);
            this.children
                    .forEach(child -> child.source
                            .forEach((key, value) -> map.putIfAbsent(key, value)));
            
            System.out.println("Update : " + map.size());
            this.data = new ImmutablePropertySource(name, map);
        }        
    }
}
