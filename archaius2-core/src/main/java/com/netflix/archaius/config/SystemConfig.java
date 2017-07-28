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
package com.netflix.archaius.config;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class SystemConfig extends AbstractConfig {

    public static final SystemConfig INSTANCE = new SystemConfig();

    public SystemConfig() {
        super("system");
    }
    
    @Override
    public Object getRawProperty(String key) {
        return System.getProperty(key);
    }

    @Override
    public Optional<Object> getProperty(String key) {
        return Optional.ofNullable(System.getProperty(key));
    }

    @Override
    public boolean containsKey(String key) {
        return System.getProperty(key) != null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Iterator<String> getKeys() {
        return getPropertyNames().iterator();
    }

    @Override
    public void forEachProperty(BiConsumer<String, Object> consumer) {
        System.getProperties().forEach((k, v) -> consumer.accept(k.toString(), v));
    }

    @Override
    public Iterable<String> getPropertyNames() { 
        return Collections.unmodifiableList(System.getProperties().keySet().stream().map(Object::toString).collect(Collectors.toList()));
    }
}
