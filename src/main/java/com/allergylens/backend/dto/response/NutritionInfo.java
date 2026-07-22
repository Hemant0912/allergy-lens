package com.allergylens.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionInfo {

  private String calories;

  private String protein;

  private String fat;

  private String carbs;

  private String sugar;
}