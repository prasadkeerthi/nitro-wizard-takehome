package org.example.nitrowizard.util;

import java.util.ArrayList;
import java.util.List;

public final class InputValidator {
    private static final int MAX_INGREDIENTS = 50;
    private static final int MAX_LENGTH = 80;

    private InputValidator() {
    }

    public static ValidationResult validateIngredients(List<String> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return ValidationResult.error("No ingredients provided.");
        }
        if (ingredients.size() > MAX_INGREDIENTS) {
            return ValidationResult.error("Too many ingredients (max " + MAX_INGREDIENTS + ").");
        }
        List<String> cleaned = new ArrayList<>();
        for (String ingredient : ingredients) {
            if (ingredient == null) {
                continue;
            }
            String trimmed = ingredient.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.length() > MAX_LENGTH) {
                return ValidationResult.error("Ingredient is too long (max " + MAX_LENGTH + " chars).");
            }
            if (!trimmed.matches("[A-Za-z0-9 '\\-]+")) {
                return ValidationResult.error("Ingredient contains invalid characters: " + trimmed);
            }
            cleaned.add(trimmed);
        }
        if (cleaned.isEmpty()) {
            return ValidationResult.error("No valid ingredients provided.");
        }
        return ValidationResult.ok(cleaned);
    }
}
