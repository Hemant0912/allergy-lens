package com.allergylens.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IngredientExplainRequest {

  @NotBlank
  private String ingredient;

}