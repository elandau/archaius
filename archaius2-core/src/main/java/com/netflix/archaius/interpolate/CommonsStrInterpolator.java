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
package com.netflix.archaius.interpolate;

import java.util.function.Function;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;

import com.netflix.archaius.api.StrInterpolator;
import com.netflix.config.api.PropertySource;

public final class CommonsStrInterpolator implements StrInterpolator {
    public static final CommonsStrInterpolator INSTANCE = new CommonsStrInterpolator();

    private CommonsStrInterpolator() {
    }
    
    @Override
    public Context create(final Lookup lookup) {
        final StrSubstitutor sub = new StrSubstitutor(
              new StrLookup<String>() {
                  @Override
                  public String lookup(String key) {
                      return lookup.lookup(key);
                  }
              }, "${", "}", '$').setValueDelimiter(":");

        return str -> sub.replace(str);
    }
    
    public static Function<String, String> forPropertySource(PropertySource source) {
        StrInterpolator.Lookup lookup = key -> source.getProperty(key).map(Object::toString).orElse(null);
        return value -> CommonsStrInterpolator.INSTANCE.create(lookup).resolve((String)value);
    }
}
