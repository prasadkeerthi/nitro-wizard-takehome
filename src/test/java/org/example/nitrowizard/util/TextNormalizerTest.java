package org.example.nitrowizard.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextNormalizerTest {
    @Test
    void normalize_trims_and_lowercases() {
        assertEquals("boomslang skin", TextNormalizer.normalize("  Boomslang Skin  "));
    }

    @Test
    void normalize_collapses_whitespace() {
        assertEquals("lacewing flies", TextNormalizer.normalize("Lacewing   Flies"));
    }

    @Test
    void normalize_handles_null() {
        assertEquals("", TextNormalizer.normalize(null));
    }

    @Test
    void normalize_handles_empty_string() {
        assertEquals("", TextNormalizer.normalize(""));
    }

    @Test
    void normalize_handles_whitespace_only() {
        assertEquals("", TextNormalizer.normalize("   "));
    }
}
