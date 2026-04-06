package org.example.nitrowizard.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SynonymConfig {
    private final Map<String, String> synonyms;

    public SynonymConfig() {
        this.synonyms = new HashMap<>();
    }

    public SynonymConfig(Map<String, String> synonyms) {
        this.synonyms = synonyms == null ? new HashMap<>() : new HashMap<>(synonyms);
    }

    public Map<String, String> getSynonyms() {
        return Collections.unmodifiableMap(synonyms);
    }

    public String apply(String normalizedName) {
        if (normalizedName == null) {
            return "";
        }
        String mapped = synonyms.get(normalizedName);
        return mapped == null ? normalizedName : mapped;
    }
}
