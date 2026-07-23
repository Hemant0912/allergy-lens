package com.allergylens.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientAnalysis {

  private String ingredient;

  private String simpleTerm;

  private boolean status;

}
