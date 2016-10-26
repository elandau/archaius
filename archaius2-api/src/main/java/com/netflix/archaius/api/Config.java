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
package com.netflix.archaius.api;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

/**
 * Core API for reading a configuration.  The API is read only.
 */
public interface Config extends ConfigNode {
    public interface Visitor<T> {
        T visitKey(Config config, String key);
    }
    
    /**
     * Register a listener that will receive a call for each property that is added, removed
     * or updated.  It is recommended that the callbacks be invoked only after a full refresh
     * of the properties to ensure they are in a consistent state.
     * 
     * @param listener
     */
    void addListener(ConfigListener listener);

    /**
     * Remove a previously registered listener.
     * @param listener
     */
    void removeListener(ConfigListener listener);

    <T> T get(Class<T> type, String key);
    
    <T> T get(Class<T> type, String key, T defaultValue);

    /**
     * Return the raw, uninterpolated, object associated with a key.
     * @param key
     */
    Object getRawProperty(String key);
    
    /**
     * Parse the property as a long.
     * @param key
     */
    Long getLong(String key);
    
    /**
     * Parse the property as a long but return a default if no property defined or the
     * property cannot be parsed successfully. 
     * @param key
     * @param defaultValue
     * @return
     */
    Long getLong(String key, Long defaultValue);

    String getString(String key);
    String getString(String key, String defaultValue);
    
    Double getDouble(String key);
    Double getDouble(String key, Double defaultValue);
    
    Integer getInteger(String key);
    Integer getInteger(String key, Integer defaultValue);
    
    Boolean getBoolean(String key);
    Boolean getBoolean(String key, Boolean defaultValue);
    
    Short getShort(String key);
    Short getShort(String key, Short defaultValue);
    
    BigInteger getBigInteger(String key);
    BigInteger getBigInteger(String key, BigInteger defaultValue);
    
    BigDecimal getBigDecimal(String key);
    BigDecimal getBigDecimal(String key, BigDecimal defaultValue);
    
    Float getFloat(String key);
    Float getFloat(String key, Float defaultValue);
    
    Byte getByte(String key);
    Byte getByte(String key, Byte defaultValue);
    
    /**
     * Get the property as a list.  Depending on the underlying implementation the list
     * may be derived from a comma delimited string or from an actual list structure.
     * @param key
     * @return
     */
    List<?> getList(String key);
    
    <T> List<T> getList(String key, Class<T> type);
    
    List<?> getList(String key, List<?> defaultValue);

    /**
     * @return Return an interator to all prefixed property names owned by this config
     */
    default Iterator<String> getKeys(String prefix) {
        return child(prefix).keys().iterator();
    }
    
    default Iterator<String> getKeys() {
        return keys().iterator();
    }
    
    /**
     * Set the Decoder used by get() to parse any type
     * @param decoder
     */
    void setDecoder(Decoder decoder);
    
    Decoder getDecoder();
    
    /**
     * Visitor pattern
     * @param visitor
     */
    <T> T accept(Visitor<T> visitor);
}
