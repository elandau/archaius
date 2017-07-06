package com.netflix.archaius.config;

import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.ConfigListener;

/**
 * Default implementation with noops for all ConfigListener events
 */
@Deprecated
public class DefaultConfigListener implements ConfigListener {
    @Override
    public void onConfigUpdated(Config config) {
    }
}
