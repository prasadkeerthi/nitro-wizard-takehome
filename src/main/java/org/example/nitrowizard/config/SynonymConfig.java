package org.example.nitrowizard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "wizard.synonyms")
public class SynonymConfig {
    private Map<String, String> map = new HashMap<>();

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map == null ? new HashMap<>() : new HashMap<>(map);
    }

    public Map<String, String> getSynonyms() {
        return Collections.unmodifiableMap(map);
    }

    public String apply(String normalizedName) {
        if (normalizedName == null) {
            return "";
        }
        String mapped = map.get(normalizedName);
        return mapped == null ? normalizedName : mapped;
    }
}
