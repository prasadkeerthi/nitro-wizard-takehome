package org.example.nitrowizard.service;

import org.example.nitrowizard.model.Elixir;

import java.util.Collections;
import java.util.List;

public class ElixirMatchResult {
    private final List<Elixir> matches;

    public ElixirMatchResult(List<Elixir> matches) {
        this.matches = matches == null ? List.of() : List.copyOf(matches);
    }

    public List<Elixir> getMatches() {
        return Collections.unmodifiableList(matches);
    }
}
