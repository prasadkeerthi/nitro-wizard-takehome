package org.example.nitrowizard.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ElixirDto {
    public String id;
    public String name;
    public String effect;
    public List<IngredientDto> ingredients;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IngredientDto {
        public String id;
        public String name;
    }
}
