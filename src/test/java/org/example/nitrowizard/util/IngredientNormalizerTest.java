package org.example.nitrowizard.util;

import org.example.nitrowizard.config.SynonymConfig;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IngredientNormalizerTest {
    @Test
    void normalize_applies_synonym_mapping() {
        SynonymConfig config = new SynonymConfig();
        config.setMap(Map.of("leech juice", "leech-juice"));
        assertEquals("leech-juice", IngredientNormalizer.normalize("Leech   Juice", config));
    }

    @Test
    void normalize_without_synonyms() {
        assertEquals("lacewing flies", IngredientNormalizer.normalize(" Lacewing  Flies ", null));
    }

    @Test
    void normalize_handles_null_input() {
        SynonymConfig config = new SynonymConfig();
        assertEquals("", IngredientNormalizer.normalize(null, config));
    }

    @Test
    void normalize_handles_empty_string() {
        SynonymConfig config = new SynonymConfig();
        assertEquals("", IngredientNormalizer.normalize("", config));
    }

    @Test
    void normalize_handles_whitespace_only() {
        SynonymConfig config = new SynonymConfig();
        assertEquals("", IngredientNormalizer.normalize("   ", config));
    }
}
