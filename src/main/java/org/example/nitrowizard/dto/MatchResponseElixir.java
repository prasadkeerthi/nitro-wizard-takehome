package org.example.nitrowizard.dto;

import java.util.List;

public record MatchResponseElixir(String name,
                                  String effect,
                                  List<String> ingredients) {
}
