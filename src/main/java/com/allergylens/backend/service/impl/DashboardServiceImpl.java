package com.allergylens.backend.service.impl;

import com.allergylens.backend.dto.response.DashboardResponse;
import com.allergylens.backend.dto.response.ProductSummary;
import com.allergylens.backend.entity.ScanHistory;
import com.allergylens.backend.repository.ProfileRepository;
import com.allergylens.backend.repository.ScanHistoryRepository;
import com.allergylens.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

  private final ProfileRepository profileRepository;
  private final ScanHistoryRepository scanHistoryRepository;

  @Override
  public DashboardResponse getDashboard(Long profileId) {

    profileRepository.findById(profileId)
        .orElseThrow(() -> new RuntimeException("Profile not found"));

    List<ScanHistory> scans = scanHistoryRepository.findByProfileIdOrderByCreatedAtDesc(profileId);

    long totalScans = scans.size();

    long safeProducts = scans.stream()
        .filter(scan -> Boolean.TRUE.equals(scan.getSafe()))
        .count();

    double safetyPercentage = totalScans == 0
        ? 0.0
        : (safeProducts * 100.0) / totalScans;

    double averageHealthScore = scans.stream()
        .map(ScanHistory::getHealthScore)
        .filter(Objects::nonNull)
        .mapToInt(Integer::intValue)
        .average()
        .orElse(0.0);

    String healthGrade;

    if (averageHealthScore >= 90) {
      healthGrade = "A+";
    } else if (averageHealthScore >= 80) {
      healthGrade = "A";
    } else if (averageHealthScore >= 70) {
      healthGrade = "B";
    } else if (averageHealthScore >= 60) {
      healthGrade = "C";
    } else if (averageHealthScore >= 40) {
      healthGrade = "D";
    } else {
      healthGrade = "F";
    }

    List<ProductSummary> safeProductsList = scans.stream()
        .filter(scan -> Boolean.TRUE.equals(scan.getSafe()))
        .map(scan -> ProductSummary.builder()
            .productName(scan.getProductName())
            .riskLevel(scan.getRiskLevel())
            .healthScore(scan.getHealthScore())
            .build())
        .toList();

    List<ProductSummary> unsafeProductsList = scans.stream()
        .filter(scan -> Boolean.FALSE.equals(scan.getSafe()))
        .map(scan -> ProductSummary.builder()
            .productName(scan.getProductName())
            .riskLevel(scan.getRiskLevel())
            .healthScore(scan.getHealthScore())
            .build())
        .toList();

    return DashboardResponse.builder()
        .totalScans(totalScans)
        .safeProducts(safeProducts)
        .safetyPercentage(Math.round(safetyPercentage * 10.0) / 10.0)
        .unsafeProducts(
            scans.stream()
                .filter(scan -> Boolean.FALSE.equals(scan.getSafe()))
                .count()
        )
        .averageHealthScore(averageHealthScore)
        .healthGrade(healthGrade)
        .mostTriggeredAllergy(
            scans.stream()
                .map(ScanHistory::getTriggeredAllergies)
                .filter(Objects::nonNull)
                .flatMap(allergies -> List.of(allergies.split(",")).stream())
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.groupingBy(a -> a, Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None")
        )
        .lowRiskProducts(
            scans.stream()
                .filter(scan -> scan.getRiskLevel() != null
                    && scan.getRiskLevel() >= 1
                    && scan.getRiskLevel() <= 3)
                .count()
        )
        .mediumRiskProducts(
            scans.stream()
                .filter(scan -> scan.getRiskLevel() != null
                    && scan.getRiskLevel() >= 4
                    && scan.getRiskLevel() <= 6)
                .count()
        )
        .highRiskProducts(
            scans.stream()
                .filter(scan -> scan.getRiskLevel() != null
                    && scan.getRiskLevel() >= 7
                    && scan.getRiskLevel() <= 10)
                .count()
        )
        .safeProductsList(safeProductsList)
        .unsafeProductsList(unsafeProductsList)
        .build();
  }
}