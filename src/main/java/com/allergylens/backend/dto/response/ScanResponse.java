package com.allergylens.backend.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResponse {

  private String productName;

  private List<String> ingredients;

  private List<IngredientAnalysis> ingredientAnalysis;

  private boolean safe;

  private Integer riskLevel;

  private String summary;

  private String recommendation;

  private List<String> alternativeProducts;

  private String confidence;

  private List<String> triggeredAllergies;

  private NutritionInfo nutrition;

  private Integer healthScore;

  private List<String> healthInsights;
}