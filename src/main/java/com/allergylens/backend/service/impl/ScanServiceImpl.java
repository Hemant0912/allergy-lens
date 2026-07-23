package com.allergylens.backend.service.impl;

import com.allergylens.backend.dto.response.BatchScanResponse;
import com.allergylens.backend.dto.response.ProfileResponse;
import com.allergylens.backend.dto.response.ScanHistoryListResponse;
import com.allergylens.backend.dto.response.ScanResponse;
import com.allergylens.backend.entity.Profile;
import com.allergylens.backend.entity.ScanHistory;
import com.allergylens.backend.repository.ProfileRepository;
import com.allergylens.backend.repository.ScanHistoryRepository;
import com.allergylens.backend.service.GeminiService;
import com.allergylens.backend.service.ScanService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;
import com.allergylens.backend.dto.response.ScanHistoryResponse;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScanServiceImpl implements ScanService {

  private final ProfileRepository profileRepository;
  private final ScanHistoryRepository scanHistoryRepository;
  private final GeminiService geminiService;
  private final ObjectMapper objectMapper;

  @Override
  public ScanResponse scan(Long profileId, List<MultipartFile> images) {

    try {

      // validate images
      if (images == null || images.isEmpty()) {
        throw new RuntimeException("Please upload at least one image.");
      }

      for (MultipartFile image : images) {
        if (image.isEmpty()) {
          throw new RuntimeException("One of the uploaded images is empty.");
        }

        if (image.getContentType() == null ||
            !image.getContentType().startsWith("image/")) {
          throw new RuntimeException("Only image files are allowed.");
        }
      }

      // validate profile
      Profile profile = profileRepository.findById(profileId)
          .orElseThrow(() -> new RuntimeException("Profile not found"));

      String geminiJson = geminiService.analyzeFoodLabel(
          images,
          profile.getAllergies()
      );

      ScanResponse response =
          objectMapper.readValue(geminiJson, ScanResponse.class);
      if ("INVALID_IMAGE".equals(response.getProductName())) {
        throw new RuntimeException(
            "Invalid image. Please upload a clear image of a packaged food product.");
      }

      System.out.println("Triggered Allergies = " + response.getTriggeredAllergies());

      ScanHistory history = ScanHistory.builder()
          .profileId(profileId)
          .productName(response.getProductName())
          .ingredients(
              response.getIngredients() == null
                  ? ""
                  : String.join(", ", response.getIngredients())
          )
          .dangerousIngredients(
              response.getDangerousIngredients() == null
                  ? ""
                  : String.join(", ", response.getDangerousIngredients())
          )
          .safe(response.isSafe())
          .riskLevel(response.getRiskLevel())
          .summary(response.getSummary())
          .recommendation(response.getRecommendation())
          .alternativeProducts(
              response.getAlternativeProducts() == null
                  ? ""
                  : String.join(", ", response.getAlternativeProducts())
          ).triggeredAllergies(
              response.getTriggeredAllergies() == null
                  ? ""
                  : String.join(", ", response.getTriggeredAllergies())
          )
          .confidence(response.getConfidence())
          .healthScore(response.getHealthScore())
          .createdAt(LocalDateTime.now())
          .build();

      scanHistoryRepository.save(history);

      return response;

    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Failed to scan image", e);
    }
  }

  @Override
  public ScanHistoryListResponse getScanHistory(Long profileId) {

    Profile profile = profileRepository.findById(profileId)
        .orElseThrow(() -> new RuntimeException("Profile not found"));

    List<ScanHistoryResponse> history = scanHistoryRepository
        .findByProfileIdOrderByCreatedAtDesc(profileId)
        .stream()
        .map(scan -> ScanHistoryResponse.builder()
            .id(scan.getId())
            .productName(scan.getProductName())
            .ingredients(
                scan.getIngredients() == null ||
                    scan.getIngredients().isBlank()
                    ? List.of()
                    : Arrays.stream(scan.getIngredients().split(","))
                        .map(String::trim)
                        .toList()
            )
            .dangerousIngredients(
                scan.getDangerousIngredients() == null ||
                    scan.getDangerousIngredients().isBlank()
                    ? List.of()
                    : Arrays.stream(scan.getDangerousIngredients().split(","))
                        .map(String::trim)
                        .toList()
            )
            .safe(scan.getSafe())
            .riskLevel(scan.getRiskLevel())
            .summary(scan.getSummary())
            .recommendation(scan.getRecommendation())
            .alternativeProducts(
                scan.getAlternativeProducts() == null ||
                    scan.getAlternativeProducts().isBlank()
                    ? List.of()
                    : Arrays.stream(scan.getAlternativeProducts().split(","))
                        .map(String::trim)
                        .toList()
            ).triggeredAllergies(
                scan.getTriggeredAllergies() == null
                    || scan.getTriggeredAllergies().isBlank()
                    ? List.of()
                    : Arrays.stream(scan.getTriggeredAllergies().split(","))
                        .map(String::trim)
                        .toList()
            )
            .confidence(scan.getConfidence())
            .createdAt(scan.getCreatedAt())
            .build())
        .toList();

    ProfileResponse profileResponse = ProfileResponse.builder()
        .id(profile.getId())
        .name(profile.getName())
        .allergies(profile.getAllergies())
        .diet(profile.getDiet())
        .createdAt(profile.getCreatedAt())
        .build();

    return ScanHistoryListResponse.builder()
        .profile(profileResponse)
        .history(history)
        .build();
  }
  @Override
  public BatchScanResponse batchScan(Long profileId, List<MultipartFile> images) {

    if (images == null || images.isEmpty()) {
      throw new RuntimeException("Please upload at least one image.");
    }

    if (images.size() > 5) {
      throw new RuntimeException("Maximum 5 products can be scanned at once.");
    }

    List<ScanResponse> responses = new ArrayList<>();

    for (MultipartFile image : images) {

      ScanResponse response = scan(profileId, List.of(image));

      responses.add(response);
    }

    long safeProducts = responses.stream()
        .filter(ScanResponse::isSafe)
        .count();

    long unsafeProducts = responses.size() - safeProducts;

    return BatchScanResponse.builder()
        .totalProducts(responses.size())
        .safeProducts((int) safeProducts)
        .unsafeProducts((int) unsafeProducts)
        .scans(responses)
        .build();
  }
}