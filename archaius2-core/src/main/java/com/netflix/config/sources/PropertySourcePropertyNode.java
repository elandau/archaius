package com.netflix.config.sources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.netflix.config.api.ConfigurationNode;
import com.netflix.config.api.PropertySource;

public class PropertySourcePropertyNode implements ConfigurationNode {
    private String path = "";
    private PropertySource source;
    
    public PropertySourcePropertyNode(PropertySource source) {
        this(source, "");
    }
    
    public PropertySourcePropertyNode(PropertySource source, String path) {
        this.source = source;
        this.path = path;
    }
    
    @Override
    public Optional<?> getValue() {
        return source.getProperty(path);
    }

    @Override
    public ConfigurationNode getChild(String key) {
        return new PropertySourcePropertyNode(source, path.isEmpty() ? key : path + "." + key);
    }
    
    @Override
    public Collection<String> children() {
        List<String> children = new ArrayList<>();
        source.getKeys(path).stream().forEach(name -> children.add(name.substring(path.length()+1)));
        return children;
    }
    
    public String toString() {
        return "PropertySourcePropertyNode[" + path + "]";
    }
}
