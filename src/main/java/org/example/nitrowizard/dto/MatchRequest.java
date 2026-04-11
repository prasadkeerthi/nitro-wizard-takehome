package org.example.nitrowizard.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MatchRequest(
        @NotEmpty(message = "ingredients is required")
        @Size(max = 50, message = "too many ingredients (max 50)")
        List<@Size(max = 80, message = "ingredient too long (max 80)") String> ingredients) {
}
