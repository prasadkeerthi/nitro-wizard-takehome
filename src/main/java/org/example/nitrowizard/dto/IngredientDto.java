package org.example.nitrowizard.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IngredientDto {
    public String id;
    public String name;
}
