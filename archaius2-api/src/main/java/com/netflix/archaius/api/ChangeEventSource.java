package com.netflix.archaius.api;

import java.util.function.Consumer;

/**
 * Contract for a type supporting change event notifications.
 */
public interface ChangeEventSource {
    interface ChangeEvent {
        PropertySource getPropertySource();
    }
    
    /**
     * Register a listener to be invoked when the configuration changes.
     * @param consumer
     */
    void addChangeEventListener(Consumer<ChangeEvent> consumer);
}
