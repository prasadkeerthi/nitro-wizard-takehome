package org.example.nitrowizard.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Elixir {
    private String id;
    private String name;
    private String effect;
    private List<String> ingredients = new ArrayList<>();

    public Elixir() {
    }

    public Elixir(String id, String name, String effect, List<String> ingredients) {
        this.id = id;
        this.name = name;
        this.effect = effect;
        setIngredients(ingredients);
    }

    public String getId() {
        return id == null ? "" : id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEffect() {
        return effect == null ? "" : effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public List<String> getIngredients() {
        return ingredients == null ? Collections.emptyList() : ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients == null ? new ArrayList<>() : new ArrayList<>(ingredients);
    }
}
