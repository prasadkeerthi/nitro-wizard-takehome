package org.example.nitrowizard.util;

public final class TextNormalizer {
    private TextNormalizer() {
    }

    public static String normalize(String input) {
        if (input == null) {
            return "";
        }
        String trimmed = input.trim().toLowerCase();
        return trimmed.replaceAll("\\s+", " ");
    }
}
