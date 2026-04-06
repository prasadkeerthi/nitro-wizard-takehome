package org.example.nitrowizard.service;

import org.example.nitrowizard.model.Elixir;

import java.util.Collections;
import java.util.List;

public class ElixirMatchResult {
    private final List<Elixir> matches;
    private final int totalElixirs;

    public ElixirMatchResult(List<Elixir> matches, int totalElixirs) {
        this.matches = matches == null ? List.of() : List.copyOf(matches);
        this.totalElixirs = totalElixirs;
    }

    public List<Elixir> getMatches() {
        return Collections.unmodifiableList(matches);
    }

    public int getTotalElixirs() {
        return totalElixirs;
    }
}
