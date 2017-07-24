package com.netflix.archaius.api;

import java.util.Collection;

/**
 * Simple for a PropertySource that is composed of multiple PropertySources.
 */
public interface CompositePropertySource extends ChangeEventSource {
    /**
     * @return All child {@link PropertySource}s in override order where the first source wins. 
     */
    Collection<PropertySource> getPropertySources();
}
