package org.example.nitrowizard.dto;

import java.util.List;

public record MatchResponse(int matchCount,
                            List<MatchResponseElixir> elixirs) {
}
