package com.allergylens.backend.dto.response;
import java.util.List;
import lombok.Data;

@Data
public class IngredientExplainResponse {

  private String ingredient;

  private String description;

  private List<String> commonUses;

  private List<String> allergyWarnings;

  private String recommendation;

}