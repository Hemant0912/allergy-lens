package com.allergylens.backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ScanHistoryResponse {

  private Long id;

  private String productName;

  private List<String> ingredients;

  private List<String> dangerousIngredients;

  private Boolean safe;

  private String riskLevel;

  private String summary;

  private String recommendation;

  private List<String> alternativeProducts;

  private List<String> triggeredAllergies;

  private String confidence;

  private LocalDateTime createdAt;
}