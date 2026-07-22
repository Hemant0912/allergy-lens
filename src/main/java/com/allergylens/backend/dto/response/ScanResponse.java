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

  private List<String> ingredients;

  private List<String> dangerousIngredients;

  private boolean safe;

  private String riskLevel;

  private String summary;
}