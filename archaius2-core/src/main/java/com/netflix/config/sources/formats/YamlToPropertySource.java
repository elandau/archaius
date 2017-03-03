package com.netflix.config.sources.formats;

import com.netflix.archaius.interpolate.CommonsStrInterpolator;
import com.netflix.config.api.PropertySource;
import com.netflix.config.sources.ImmutablePropertySource;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Function to load a YAML file for a given URL into an ImmutablePropertySource.
 * 
 * YAML being hierarchical (as opposed to a flat .properties files) is amenable to 
 * scoping of properties via a simple expression that is part of the key.  The expression 
 * is simply evaluated by matching any property value (normally something like 'netflix.environment', 
 * 'netflix.stack', 'netflix.region', etc).  A property or subset of properties will only be loaded
 * if the condition is true.
 * 
 * To scope properties simply specify the scope in [] as part of any key in a property 
 * hierarchy.  For example, use application.timeout[${netflix.environment}=test] to scope the property 
 * 'application.timeout' to the test environment.  The scope may be specified at any level.
 * For example application[${netflix.environment}=test].timeout would yield similar results except for that 
 * all sub properties of application[${netflix.environment}=test] will be scope to the test environment
 * 
 * When multiple scoped properties are valid the last matched value in the file wins.  Scopes 
 * should therefore be specified in order from least specific to most specific.
 * 
 * For example, consider a property application.timeout that is dependent on the environment and 
 * stack.  This can be specified as follows,
 * 
 * <pre>
 *  application : 
 *      timeout : 10
 *  application[${netflix.environment}=test] : 
 *      timeout : 100
 *  application[${netflix.environment}=test][${netflix.stack}=batch]
 *      timeout : 1000
 *  application[${netflix.environment}=prod] :
 *      timeout : 10
 * </pre>
 * 
 * In the above example for netflix.environment=test and netflix.stack=batch the valid values of application.timeout would be
 * [10, 100, 1000] where 1000 wins as it is last to match in the file and most specific.
 * 
 * Since the scope may be specified at any level the above example can be rewritten as, 
 * <pre>
 *  application :
 *      timeout : 10
 *      timeout[${netflix.environment}=test] : 100
 *      timeout[${netflix.environment}=test][${netflix.stack}=batch] : 1000
 *      timeout[${netflix.environment}=prod] : 10
 * </pre>
 * 
 * Or,
 * <pre>
 *  application.timeout : 10
 *  application.timeout[${netflix.environment}=test] : 100
 *  application.timeout[${netflix.environment}=test][${netflix.stack}=batch] : 1000
 *  application.timeout[${netflix.environment}=prod] : 10
 * </pre>
 * 
 * Choice of styles to use depends on the complexity of the configuration and what works 
 * best for developer productivity.  For example, the first example would be ideal when there 
 * are many properties and it is desirable to have all properties grouped by scope.  The 
 * latter makes searching for the full property name "application.timeout" easier.
 * 
 */
public class YamlToPropertySource implements PropertySourceLoader {
    private static final Logger LOG = LoggerFactory.getLogger(YamlToPropertySource.class);
    
    private static final String CONDITION_NOT_MET = "<condition_not_met>";

    /**
     * Replacement evaluator for the syntax [key=value] that will remove the entire replacement
     * if evaluated to true or replace it with {@link CONDITION_NOT_MET}
     */
    private static StrSubstitutor evaluatorSubstitutor = new StrSubstitutor(
            new StrLookup<String>() {
                @Override
                public String lookup(String key) {
                    String[] parts = key.split("=");
                    return parts.length == 2 
                        ? (parts[0].equals(parts[1])) ? "" : CONDITION_NOT_MET
                        : CONDITION_NOT_MET;
                }
            }, "[", "]", '[');
    private static Function<String, String> evaluator = (str) -> evaluatorSubstitutor.replace(str);

    private final Function<String, String> interpolator;

    public YamlToPropertySource(PropertySource sourceForInterpolation) {
        this.interpolator = CommonsStrInterpolator.forPropertySource(sourceForInterpolation);
    }
    
    @Override
    public PropertySource apply(URL t) {
        Yaml yaml = new Yaml();
        
        try (InputStream is = t.openStream()) {
            Map<String, Object> values = (Map<String, Object>) yaml.load(is);

            ImmutablePropertySource.Builder builder = ImmutablePropertySource.builder()
                    .named(t.toExternalForm());
            
            traverse("", "", values, (key, value) -> {
                // Strip extra spaces from key -> interpolate values -> evaluate equalities
                String evaluatedKey = evaluator.apply(interpolator.apply(key.replaceAll(" ", "")));
                if (!evaluatedKey.contains(CONDITION_NOT_MET)) {
                    builder.put(evaluatedKey, value);
                } else {
                    LOG.debug("{} : Discarding property {}", t.toExternalForm(), key);
                }
            });
                    
            return builder.build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from " + t.toExternalForm());
        }
    }
    
    /**
     * Recursively traverse a path in the YAML calling the consumer when a leaf is reached
     * 
     * @param propertyName
     * @param key
     * @param obj
     * @param consumer
     */
    void traverse(String propertyName, String key, Object obj, BiConsumer<String, Object> consumer) {
        String newName = propertyName.isEmpty() ? key : propertyName + "." + key;
        if (obj instanceof Map) {
            ((Map<String, Object>)obj).forEach((k, v) -> traverse(newName, k, v, consumer));
        } else if (obj instanceof List) {
            int index = 0;
            for (Object element : (List)obj) {
                traverse(newName, Integer.toString(index), element, consumer);
                index++;
            }
        } else {
            consumer.accept(newName, obj);
        }
    }
}
