package org.example.nitrowizard.util;

import java.util.List;

public record ValidationResult(List<String> cleanedIngredients, String errorMessage) {
    public static ValidationResult ok(List<String> cleanedIngredients) {
        return new ValidationResult(cleanedIngredients, null);
    }

    public static ValidationResult error(String message) {
        return new ValidationResult(List.of(), message);
    }

    public boolean isValid() {
        return errorMessage == null || errorMessage.isBlank();
    }
}
