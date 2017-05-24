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
package com.netflix.archaius.readers;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.archaius.api.StrInterpolator;

@Deprecated
public class PropertiesConfigReader {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesConfigReader.class);
    
    private static final String INCLUDE_KEY = "@next";
    private static final String SUFFIX = ".properties";
    
    private void internalLoad(Properties props, Set<String> seenUrls, ClassLoader loader, URL url, StrInterpolator strInterpolator, StrInterpolator.Lookup lookup) {
        LOG.debug("Attempting to load : {}", url.toExternalForm());
        // Guard against circular dependencies 
        if (!seenUrls.contains(url.toExternalForm())) {
            seenUrls.add(url.toExternalForm());
            
            try {
                // Load properties into the single Properties object overriding any property
                // that may already exist
                Map<String, String> p = new URLConfigReader(url).call().getToAdd();
                LOG.debug("Loaded : {}", url.toExternalForm());
                props.putAll(p);
    
                // Recursively load any files referenced by an @next property in the file
                // Only one @next property is expected and the value may be a list of files
                String next = p.get(INCLUDE_KEY);
                if (next != null) {
                    p.remove(INCLUDE_KEY);
                    for (String urlString : next.split(",")) {
//                        URL nextUrl = getResource(loader, strInterpolator.create(lookup).resolve(urlString));
//                        if (nextUrl != null) {
//                            internalLoad(props, seenUrls, loader, nextUrl, strInterpolator, lookup);
//                        }
                    }
                }
            } catch (IOException e) {
                LOG.debug("Unable to load configuration file {}. {}", url, e.getMessage());
            }
        }
        else {
            LOG.debug("Circular dependency trying to load url : {}", url.toExternalForm());
        }
    }
}
