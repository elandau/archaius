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
package com.netflix.archaius.commons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.commons.configuration.AbstractConfiguration;

import com.netflix.config.api.PropertySource;

/**
 * Adaptor to allow an Apache Commons Configuration AbstractConfiguration to be used
 * as an Archaius2 PropertySource
 */
public class CommonsToPropertySource implements PropertySource {

    private final AbstractConfiguration config;
    private final String name;
    
    public CommonsToPropertySource(String name, AbstractConfiguration config) {
        this.config = config;
        this.name = name;
    }

    public CommonsToPropertySource(AbstractConfiguration config) {
        this("", config);
    }

    @Override
    public boolean isEmpty() {
        return config.isEmpty();
    }

//    @Override
//    public <T> List<T> getList(String key, Class<T> type) {
//        List value = config.getList(key);
//        if (value == null) {
//            return notFound(key);
//        }
//;
//        List<T> result = new ArrayList<T>();
//        for (Object part : value) {
//            if (type.isInstance(part)) {
//                result.add((T)part);
//            } else if (part instanceof String) {
//                result.add(getDecoder().decode(type, (String) part));
//            } else {
//                throw new UnsupportedOperationException(
//                        "Property values other than " + type.getCanonicalName() +" or String not supported");
//            }
//        }
//        return result;
//    }

//    @Override
//    public List getList(String key) {
//        List value = config.getList(key);
//        if (value == null) {
//            return notFound(key);
//        }
//        return value;
//    }

//    @Override
//    public String getString(String key, String defaultValue) {
//        List value = config.getList(key);
//        if (value == null) {
//            return notFound(key, defaultValue != null ? getStrInterpolator().create(getLookup()).resolve(defaultValue) : null);
//        }
//        List<String> interpolatedResult = new ArrayList<>();
//        for (Object part : value) {
//            if (part instanceof String) {
//                interpolatedResult.add(getStrInterpolator().create(getLookup()).resolve(part.toString()));
//            } else {
//                throw new UnsupportedOperationException(
//                        "Property values other than String not supported");
//            }
//        }
//        return StringUtils.join(interpolatedResult, getListDelimiter());
//    }
//
//    @Override
//    public String getString(String key) {
//        List value = config.getList(key);
//        if (value == null) {
//            return notFound(key);
//        }
//        List<String> interpolatedResult = new ArrayList<>();
//        for (Object part : value) {
//            if (part instanceof String) {
//                interpolatedResult.add(getStrInterpolator().create(getLookup()).resolve(part.toString()));
//            } else {
//                throw new UnsupportedOperationException(
//                        "Property values other than String not supported");
//            }
//        }
//        return StringUtils.join(interpolatedResult, getListDelimiter());
//    }

    @Override
    public void forEach(BiConsumer<String, Object> consumer) {
        config.getKeys().forEachRemaining((key) -> consumer.accept(key, config.getProperty(key)));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<Object> getProperty(String key) {
        return Optional.ofNullable(config.getProperty(key));
    }

    @Override
    public Collection<String> getKeys() {
        Collection<String> list = new ArrayList<>();
        config.getKeys().forEachRemaining(key -> list.add(key));
        return list;
    }

    @Override
    public Collection<String> getKeys(String prefix) {
        Collection<String> list = new ArrayList<>();
        config.getKeys().forEachRemaining(key -> {
            if (key.startsWith(prefix)) 
                list.add(key);
        });
        return list;
    }

    @Override
    public int size() {
        return 0;  // TODO:
    }

    @Override
    public PropertySource snapshot() {
        return this;
    }
}
