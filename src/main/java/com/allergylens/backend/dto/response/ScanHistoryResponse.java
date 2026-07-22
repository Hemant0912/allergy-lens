package com.allergylens.backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScanHistoryResponse {

  private Long id;

  private List<String> ingredients;

  private List<String> dangerousIngredients;

  private Boolean safe;

  private String riskLevel;

  private String summary;

  private LocalDateTime createdAt;
}