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
public class DashboardResponse {

  private Long totalScans;

  private Long safeProducts;

  private Long unsafeProducts;

  private Double safetyPercentage;

  private Double averageHealthScore;

  private String healthGrade;

  private String mostTriggeredAllergy;

  private Long lowRiskProducts;

  private Long mediumRiskProducts;

  private Long highRiskProducts;

  private List<ProductSummary> safeProductsList;

  private List<ProductSummary> unsafeProductsList;
}