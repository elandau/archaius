package com.netflix.config.sources;

import com.netflix.archaius.internal.WeakReferenceSet;
import com.netflix.config.api.PropertySource;

import java.util.function.Consumer;

/**
 * 
 */
public class PollingPropertySource extends DelegatingPropertySource {
    private final Runnable cancellation;
    private final WeakReferenceSet<Consumer<PropertySource>> listeners = new WeakReferenceSet<>();
    private volatile PropertySource delegate;
    
    /**
     * Async supplier of PropertySource instances
     */
    public interface PropertySourceSupplier {
        Runnable get(Consumer<PropertySource> consumer);
    }
    
    /**
     * Strategy driving when to poll
     */
    public interface Strategy {
        Runnable start(Runnable runnable);
    }
    
    public PollingPropertySource(Strategy strategy, Consumer<Consumer<PropertySource>> source) {
        // TODO: Cancel the source consumer
        cancellation = strategy.start(() -> source.accept(s -> setSource(s)));
    }
    
    private void setSource(PropertySource source) {
        this.delegate = source;
    }
    
    public void shutdown() {
        cancellation.run();
    }

    @Override
    public AutoCloseable addListener(Consumer<PropertySource> listener) {
        return listeners.add(listener, this);
    }

    protected void notifyListeners() {
        listeners.forEach(listener -> listener.accept(this));
    }
    
    @Override
    protected PropertySource delegate() {
        return delegate;
    }
}
