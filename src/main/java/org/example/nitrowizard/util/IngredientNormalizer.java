package org.example.nitrowizard.util;

import org.example.nitrowizard.config.SynonymConfig;

public final class IngredientNormalizer {
    private IngredientNormalizer() {
    }

    public static String normalize(String input, SynonymConfig synonymConfig) {
        String normalized = TextNormalizer.normalize(input);
        if (synonymConfig == null) {
            return normalized;
        }
        return synonymConfig.apply(normalized);
    }
}
