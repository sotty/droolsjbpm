package org.drools.io.impl;

import org.drools.builder.ResourceType;
import org.drools.builder.ResourceConfiguration;
import org.drools.io.Resource;

public class KnowledgeResource {
    private String                source;
    private ResourceType         type;
    private ResourceConfiguration configuration;

    public KnowledgeResource(String src,
                             ResourceType type) {
        this.source = src;
        this.type = type;
    }

    public KnowledgeResource(String src,
                             ResourceType type,
                             ResourceConfiguration configuration) {
        this.source = src;
        this.type = type;
        this.configuration = configuration;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String src) {
        this.source = src;
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public ResourceConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ResourceConfiguration configuration) {
        this.configuration = configuration;
    }

}
