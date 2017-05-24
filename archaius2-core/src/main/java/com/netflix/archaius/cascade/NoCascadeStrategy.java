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
package com.netflix.archaius.cascade;

import com.netflix.archaius.api.CascadeStrategy;
import com.netflix.archaius.api.StrInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Default 'noop' cascade strategy returns only the original resource name
 */
public class NoCascadeStrategy implements CascadeStrategy {
    public static final CascadeStrategy INSTANCE = new NoCascadeStrategy();

    @Override
    public List<String> generate(String name, StrInterpolator interpolator, StrInterpolator.Lookup config) {
        List<String> list = new ArrayList<String>();
        list.add(name);
        return list;
    }
}
